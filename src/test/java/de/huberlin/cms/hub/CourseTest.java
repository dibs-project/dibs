/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.huberlin.cms.hub.AllocationRule;
import de.huberlin.cms.hub.Course;

/**
 * @author Markus Michler
 */
public class CourseTest extends HubTest {
    @Test
    public void testCreateAllocationRule() {
        Course course = this.service.createCourse("Computer Science", 500, null);
        AllocationRule rule = course.createAllocationRule(null);
        assertEquals(rule, course.getAllocationRule());
    }
}
