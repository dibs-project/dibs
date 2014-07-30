package de.huberlin.cms.hub;

import static org.apache.commons.collections4.ListUtils.select;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections4.Predicate;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Michler
 */
public class ApplicationTest extends HubTest {
    private Application application;

    @Before
    public void before() throws Exception {
        application = course.apply(user.getId(), user);
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

        // TODO: this.application.getEvaluationByCriterionId("qualification");
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("required_information_type_id", "qualification");
        for (Evaluation evaluation : this.application.getEvaluations(filter, null)) {
            assertEquals(information, evaluation.getInformation());
            assertEquals(Evaluation.STATUS_EVALUATED, evaluation.getStatus());
            assertNotNull(evaluation.getValue());
        }
    }

    @Test
    public final void testSetStatus() {
        String newStatus = Application.STATUS_COMPLETE;
        application.setStatus(newStatus, null);
        application = service.getApplication(application.getId());
        assertEquals(application.getStatus(), newStatus);
    }
}
