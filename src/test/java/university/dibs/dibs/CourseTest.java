/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import university.dibs.dibs.AllocationRule;
import university.dibs.dibs.Application;
import university.dibs.dibs.Evaluation;
import university.dibs.dibs.Information;
import university.dibs.dibs.DibsException.IllegalStateException;

public class CourseTest extends DibsTest {
    @Test
    public void testCreateAllocationRule() {
        course.unpublish(null);
        AllocationRule allocationRule = course.createAllocationRule(null);
        assertEquals(allocationRule, course.getAllocationRule());
    }

    @Test
    public void testCreateAllocationRulePublished() {
        exception.expect(IllegalStateException.class);
        course.createAllocationRule(null);
    }

    @Test
    public void testApply() {
        Application application = course.apply(user.getId(), null);
        Evaluation evaluation = application.getEvaluationByCriterionId("qualification");
        assertTrue(user.getApplications().contains(application));
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
        exception.expect(IllegalStateException.class);
        course.unpublish(null);
        course.apply(user.getId(), null);
    }

    @Test
    public void testPublishIncomplete() {
        exception.expect(IllegalStateException.class);
        course = this.service.createCourse("Computer Science", 500, false, null);
        course.publish(null);
    }

    @Test
    public void testUnpublishApplicationPresent() {
        exception.expect(IllegalStateException.class);
        course.apply(user.getId(), null);
        course.unpublish(null);
    }

    @Test
    public void testStartAdmission() {
        String applicationId = course.apply(user.getId(), null).getId();
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        user.createInformation("qualification", args, null);
        course.startAdmission(null);
        assertTrue(service.getCourse(course.getId()).isAdmission());
        assertEquals(Application.STATUS_ADMITTED, service.getApplication(applicationId)
            .getStatus());
    }

    @Test
    public void testStartAdmissionUnpublished() {
        exception.expect(IllegalStateException.class);
        course.unpublish(null);
        course.startAdmission(null);
    }
}
