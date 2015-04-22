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

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        Map<String, Object> args = new HashMap<String, Object>();
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
    public void testApplyUserAlreadyApplied() {
        exception.expect(IllegalStateException.class);
        course.apply(user.getId(), null);
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
        Map<String, Object> args = new HashMap<String, Object>();
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
