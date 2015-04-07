/*
 * dibs
 * Copyright (C) 2015  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.junit.Before;
import org.junit.Test;

import java.io.IOError;
import java.sql.SQLException;

public class ApplicationServiceTransactionTest extends DibsTest {
    @Before
    public void before() throws SQLException {
        new QueryRunner().update(db, "DROP TABLE IF EXISTS _tmp; CREATE TABLE _tmp (value VARCHAR(256))");
        new QueryRunner().update(db, "INSERT INTO _tmp (value) VALUES (NULL)");
    }

    @Test
    public final void testTransactionIllegalState() {
        this.exception.expect(DibsException.IllegalStateException.class);

        this.service.beginTransaction();
        this.service.endTransaction();
        this.service.endTransaction();
    }

    @Test
    public final void testTransactionNested() {
        this.service.beginTransaction();
        this.setValue("a");

        this.service.beginTransaction();
        this.setValue("b");
        this.service.endTransaction();

        this.setValue("c");
        this.service.endTransaction();

        assertEquals("c", this.getValue());
    }

    private void setValue(String value) {
        try {
            new QueryRunner().update(db, "UPDATE _tmp SET value = ?", value);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private String getValue() {
        try {
            return new QueryRunner().query(db, "SELECT * FROM _tmp",
                new ScalarHandler<String>("value"));
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
