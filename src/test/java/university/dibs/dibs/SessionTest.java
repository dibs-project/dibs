/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.Session;

public class SessionTest extends DibsTest {
    private Session session;

    @Before
    public void before() {
        this.session = this.service.login(this.user.getCredential(), "localhost");
    }

    @Test
    public void testEnd() {
        Date endTime = this.session.getEndTime();
        this.session.end();
        assertFalse(this.session.isValid());
        assertTrue(this.session.getEndTime().before(endTime));
    }

    @Test
    public void testEndExpired() {
        this.session.end();
        Date endTime = this.session.getEndTime();
        this.session.end();
        assertFalse(this.session.isValid());
        assertEquals(endTime, this.session.getEndTime());
    }
}
