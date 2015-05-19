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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import university.dibs.dibs.DibsException.IllegalStateException;
import university.dibs.dibs.DibsException.ObjectNotFoundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApplicationServiceTest extends DibsTest {
    @Test
    public void testSetupStorageNonEmptyDatabase() {
        this.exception.expect(IllegalStateException.class);
        ApplicationService.setupStorage(this.db);
    }

    @Test
    public void testCreateUser() {
        String email = "moss@example.org";
        User user = this.service.createUser("Maurice", email, email + ":secr3t",
            User.ROLE_APPLICANT);
        assertEquals(email, user.getEmail());
        assertTrue(this.service.getUsers().contains(user));
    }

    @Test
    public void testCreateUserEmptyEmail() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("email");
        this.service.createUser("Maurice", "", "moss@example.org:secre3t",
            User.ROLE_APPLICANT);
    }

    @Test
    public void testCreateUserDuplicatedEmail() {
        this.exception.expect(IllegalStateException.class);
        this.service.createUser("Maurice", "moss@example.org", "moss@example.org:secre3t",
            User.ROLE_APPLICANT);
        this.service.createUser("Maurice", "moss@example.org", "moss@example.org:secre3t",
            User.ROLE_APPLICANT);
    }
    @Test
    public void testGetUser() {
        assertEquals(this.user, this.service.getUser(this.user.getId()));
    }

    @Test
    public void testGetUserNonExisting() {
        this.exception.expect(ObjectNotFoundException.class);
        this.service.getUser("foo");
    }

    @Test
    public void testGetApplicationNonExisting() {
        this.exception.expect(ObjectNotFoundException.class);
        this.service.getApplication("foo");
    }

    @Test
    public void testAuthenticate() {
        User user = this.service.authenticate(this.user.getCredential());
        assertNotNull(user);
        assertEquals(this.user, user);
    }

    @Test
    public void testAuthenticateBadCredential() {
        assertNull(this.service.authenticate("foo:bar"));
    }

    @Test
    public void testLogin() {
        Session session = this.service.login(this.user.getCredential(), "localhost");
        assertNotNull(session);
        assertEquals(this.user, session.getUser());
        assertTrue(session.isValid());
    }

    @Test
    public void testLoginBadCredential() {
        assertNull(this.service.login("foo:bar", "localhost"));
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
        String name = "Computer Science";
        Course course = this.service.createCourse(name, 200, false, null);
        assertEquals(name, course.getName());
        assertTrue(this.service.getCourses().contains(course));
    }

    @Test
    public void testCreateCourseEmptyName() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("name");
        this.service.createCourse("", 200, false, null);
    }

    @Test
    public void testCreateCourseNonpositiveCapacity() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("capacity");
        this.service.createCourse("Computer Science", -3, false, null);
    }

    @Test
    public void testGetCourse() {
        assertEquals(this.course, this.service.getCourse(this.course.getId()));
    }

    @Test
    public void testGetCourseNonExisting() {
        this.exception.expect(ObjectNotFoundException.class);
        this.service.getCourse("foo");
    }

    @Test
    public void testGetCoursesFilter() {
        // unpublished course
        Course course =
            this.service.createCourse("Science of Computer", 500, false, null);
        Map<String, Object> filter = new HashMap<>();
        filter.put("published", false);
        assertEquals(Arrays.asList(course), this.service.getCourses(filter));
    }

    @Test
    public void testGetQuotaNonExisting() {
        this.exception.expect(ObjectNotFoundException.class);
        this.service.getQuota("foo");
    }

    @Test
    public void testGetCriteriaFilter() {
        Map<String, Object> filter = new HashMap<>();
        filter.put("required_information_type_id", "qualification");
        assertEquals(Arrays.asList(this.service.getCriterion("qualification")),
                     this.service.getCriteria(filter, null));
    }
}
