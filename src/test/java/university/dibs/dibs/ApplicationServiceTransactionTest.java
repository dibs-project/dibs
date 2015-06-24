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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ApplicationServiceTransactionTest extends DibsTest {
    private Connection db2;
    private QueryRunner queryRunner;

    @Before
    public void before() throws SQLException {
        this.queryRunner = new QueryRunner();
        this.queryRunner.update(this.db, "DROP TABLE IF EXISTS _tmp");
        this.queryRunner.update(this.db, "CREATE TABLE _tmp (value VARCHAR(256))");
        this.queryRunner.update(this.db, "INSERT INTO _tmp (value) VALUES (NULL)");

        this.db2 = DriverManager.getConnection(this.config.getProperty("db_url"),
            this.config.getProperty("db_user"),
            this.config.getProperty("db_password"));
    }

    @Test
    public void testEndTransactionBeginMissing() {
        this.exception.expect(DibsException.IllegalStateException.class);

        this.service.endTransaction();
    }

    @Test
    public void testNestedTransaction() throws SQLException {
        this.service.beginTransaction();

        this.service.beginTransaction();
        this.queryRunner.update(this.db, "UPDATE _tmp SET value = ?", "x");
        this.service.endTransaction();

        assertEquals("x", this.getValue(this.db));
        assertEquals(null, this.getValue(this.db2));

        this.service.endTransaction();

        assertEquals("x", this.getValue(this.db));
        assertEquals("x", this.getValue(this.db2));
    }

    private String getValue(Connection db) {
        try {
            return this.queryRunner.query(db, "SELECT * FROM _tmp", new ScalarHandler<String>(
                "value"));
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
