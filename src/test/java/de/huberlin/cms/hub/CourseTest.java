/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import de.huberlin.cms.hub.AllocationRule;

public class CourseTest extends HubTest {
    @Test
    public void testCreateAllocationRule() {
        AllocationRule rule = course.createAllocationRule(null);
        assertEquals(rule, course.getAllocationRule());
    }

    @Test
    public final void testApply() {
        Application application = course.apply(user.getId(), null);
        assertTrue(user.getApplications(null).contains(application));
        assertTrue(application.getEvaluations(null).size() > 0);
    }
}
