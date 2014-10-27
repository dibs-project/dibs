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

import de.huberlin.cms.hub.HubException.IllegalStateException;

public class CourseTest extends HubTest {
    @Test
    public void testCreateAllocationRulePublished() {
        exception.expect(IllegalStateException.class);
        course.createAllocationRule(null);
    }

    @Test
    public void testApply() {
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
    public void testApplyUnpublished() {
        course.unpublish(null);
        exception.expect(IllegalStateException.class);
        course.apply(user.getId(), null);
    }

    @Test
    public void testPublishIncomplete() {
        exception.expect(IllegalStateException.class);
        course = this.service.createCourse("Computer Science", 500, "cs", "bsc", null);
        course.publish(null);
    }

    @Test
    public void testUnpublishApplied() {
        exception.expect(IllegalStateException.class);
        course.apply(user.getId(), null);
        course.unpublish(null);
    }
}
