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
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import university.dibs.dibs.DibsException;
import university.dibs.dibs.Information;
import university.dibs.dibs.Qualification;

public class UserTest extends DibsTest {
    @Test
    public void testCreateInformation() {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        Information information =
            this.user.createInformation("qualification", args, null);
        assertTrue(information instanceof Qualification);
        assertEquals(args.get("grade"), ((Qualification) information).getGrade());
    }

    @Test
    public void testCreateInformationUnknownType() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("typeId");
        this.user.createInformation("_foo", new HashMap<String, Object>(), null);
    }

    @Test
    public void testGetInformationByType() {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        Information information = user.createInformation("qualification", args, null);
        assertEquals(information, user.getInformationByType("qualification"));
    }

    @Test
    public void testGetInformationByTypeUnknownType() {
        exception.expect(IllegalArgumentException.class);
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        user.createInformation("foo", args, null);
    }

    @Test
    public void testGetInformationByTypeNonExisting() {
        exception.expect(DibsException.ObjectNotFoundException.class);
        user.getInformationByType("qualification");
    }
}
