/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

public class CourseTest extends HubTest {
    @Test
    public void testCreateAllocationRule() {
        AllocationRule rule = course.createAllocationRule(null);
        assertEquals(rule, course.getAllocationRule());
    }

    @Test
    public final void testApply() {
        Application application = course.apply(user.getId(), null);
        Evaluation evaluation = application.getEvaluationByCriterionId("qualification");
        assertTrue(user.getApplications(null).contains(application));
        assertEquals(Evaluation.STATUS_INFORMATION_MISSING, evaluation.getStatus());
        assertNull(evaluation.getInformation());
        assertNull(evaluation.getValue());
    }

    @Test
    public void testApplyExistingInformation() {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        Information information =
            this.user.createInformation("qualification", args, null);

        Application application = course.apply(user.getId(), null);
        Evaluation evaluation = application.getEvaluationByCriterionId("qualification");
        assertEquals(Evaluation.STATUS_EVALUATED, evaluation.getStatus());
        assertEquals(information, evaluation.getInformation());
        assertNotNull(evaluation.getValue());
    }

    @Test
    public void testGetApplications() {
        User user1 = this.service.createUser("Maurice", "maurice@moss.net");
        User user2 = this.service.createUser("Peter", "peter@pan.com");
        Application a1 = course.apply(user1.getId(), null);
        Application a2 = course.apply(user2.getId(), null);
        List<Application> applications = new ArrayList<Application>();
        applications.add(a1);
        applications.add(a2);
        assertEquals(applications, this.service.getCourse(this.course.getId()).getApplications());
        assertTrue(this.service.getCourse(this.course.getId()).getApplications().contains(a1));
        assertTrue(this.service.getCourse(this.course.getId()).getApplications().contains(a2));
    }

}
