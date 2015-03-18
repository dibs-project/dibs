/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import static org.apache.commons.collections4.ListUtils.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.Predicate;
import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.Application;
import university.dibs.dibs.Evaluation;
import university.dibs.dibs.Information;
import university.dibs.dibs.DibsException.IllegalStateException;

public class ApplicationTest extends DibsTest {
    private Application application;

    @Before
    public void before() throws Exception {
        course.publish(null);
        application = course.apply(user.getId(), user);
    }

    @Test
    public void testAccept() {
        Map<String, Object> args = new HashMap<>();
        args.put("grade", 2.0);
        user.createInformation("qualification", args, null);
        course.startAdmission(null);
        application = service.getApplication(application.getId());
        application.accept();
        assertEquals(Application.STATUS_CONFIRMED, application.getStatus());
    }

    @Test
    public void testAcceptNotAdmitted() {
        exception.expect(IllegalStateException.class);
        service.getApplication(application.getId()).accept();
    }

    @Test
    public void testGetEvaluationsFilter() {
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("required_information_type_id", "qualification");
        List<Evaluation> evaluations = select(this.application.getEvaluations(null),
            new Predicate<Evaluation>() {
                public boolean evaluate(Evaluation object) {
                    return object.getCriterion().getRequiredInformationType().getId()
                        .equals("qualification");
                }
            });
        assertEquals(evaluations, this.application.getEvaluations(filter, null));
    }

    @Test
    public void testUserInformationCreated() {
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        Information information =
            this.user.createInformation("qualification", args, null);

        Evaluation evaluation =
            this.application.getEvaluationByCriterionId("qualification");
        assertEquals(Evaluation.STATUS_EVALUATED, evaluation.getStatus());
        assertEquals(information, evaluation.getInformation());
        assertNotNull(evaluation.getValue());
    }

    @Test
    public final void testSetStatus() {
        String newStatus = Application.STATUS_COMPLETE;
        application.setStatus(newStatus, true, null);
        application = service.getApplication(application.getId());
        assertEquals(application.getStatus(), newStatus);
    }
}
