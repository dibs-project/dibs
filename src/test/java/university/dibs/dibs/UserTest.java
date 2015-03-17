/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
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
