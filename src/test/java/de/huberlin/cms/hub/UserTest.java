/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

/**
 * @author Sven Pfaller
 */
public class UserTest extends HubTest {
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
}
