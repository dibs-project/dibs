/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

/**
 * @author Markus Michler
 * @author Sven Pfaller
 */
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
}
