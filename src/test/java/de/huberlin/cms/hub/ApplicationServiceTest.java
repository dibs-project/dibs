/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

/**
 * @author Sven Pfaller
 */
public class ApplicationServiceTest extends HubTest {
    @Test
    public void testCreateUser() {
        String email = "moss@example.org";
        User user = this.service.createUser("Maurice", email);
        assertEquals(email, user.getEmail());
    }

    @Test
    public void testCreateUserEmptyEmail() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("email");
        this.service.createUser("Maurice", "");
    }

    @Test
    public void testGetUser() {
        User user = this.service.getUser(this.user.getId());
        assertEquals(this.user.getId(), user.getId());
    }

    @Test
    public void testGetUserNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        this.service.getUser("foo");
    }

    @Test
    public void testGetUsers() {
        List<User> users = this.service.getUsers();
        assertEquals(this.user.getId(), users.get(0).getId());
    }

    @Test
    public void testSetSemester() {
        String semester = "2222SS";
        service.setSemester(semester);
        assertEquals(semester, service.getSettings().getSemester());
    }

    @Test
    public void testGetSettings() {
        service.getSettings();
    }
}
