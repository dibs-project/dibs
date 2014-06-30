/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.huberlin.cms.hub.AllocationRule;
import de.huberlin.cms.hub.Course;

public class CourseTest extends HubTest {
    AllocationRule rule;

    @Test
    public void testCreateAllocationRule() {
        rule = Course.createAllocationRule(null);
        assertEquals(rule.id, rule.getId());
        assertTrue(rule.equals(this.service.getAllocationRule(rule.getId())));
    }
}
