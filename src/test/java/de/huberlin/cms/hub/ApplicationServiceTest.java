/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Sven Pfaller
 * @author Phuong Anh Ha
 */
public class ApplicationServiceTest extends HubTest {
    Course course;

    @Before
    public void before() {
        this.course = this.service.createCourse("Informatik", 500, this.user);
    }

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

    @Test
    public void testCreateCourse() {
        String name = "Jura";
        Course course = this.service.createCourse(name, 200, this.user);
        assertEquals(name, course.getName());
    }

    @Test
    public void testCreateCourseEmptyName() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("name");
        this.service.createCourse("", -3, null);
    }

    @Test
    public void testCreateCourseNegativCapacity() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("capacity");
        this.service.createCourse("Informatik", -3, null);
    }

    @Test
    public void testGetCourse() {
        Course testCourse = this.service.getCourse(this.course.getId());
        assertEquals(this.course.getName(), testCourse.getName());
    }

    @Test
    public void testGetCourseNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        this.service.getCourse("foo");
    }
}
