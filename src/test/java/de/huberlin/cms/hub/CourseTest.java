/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.AllocationRule;
import de.huberlin.cms.hub.Course;

/**
 * @author Markus Michler
 */
public class CourseTest extends HubTest {
    private Course course;

    @Before
    public void before() {
        course = service.createCourse("Computer Science", 500, null);
    }

    @Test
    public void testCreateAllocationRule() {
        AllocationRule rule = course.createAllocationRule(null);
        assertEquals(rule, course.getAllocationRule());
    }

    @Test
    public final void testApply() {
        Application application = course.apply(user.getId(), null);
        assertTrue(user.getApplications(null).contains(application));
    }
}