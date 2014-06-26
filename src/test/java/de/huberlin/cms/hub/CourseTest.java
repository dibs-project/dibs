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

public class CourseTest extends HubTest {
    private Course course;
    private AllocationRule rule;

    @Before
    public void before() {
        this.course = this.service.createCourse("Computer Science", 500, null);
        this.rule = this.course.createAllocationRule(null);
    }

    @Test
    public void testCreateAllocationRule() {
        AllocationRule rule = this.course.createAllocationRule(null);
        assertEquals(rule.id, rule.getId());
        assertTrue(this.service.getAllocationRule(rule.id).equals(rule));
    }
}
