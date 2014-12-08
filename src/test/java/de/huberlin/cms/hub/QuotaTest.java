/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class QuotaTest extends HubTest {
    private Quota quota;
    private Application incompleteApplication;
    private List<Application> validApplications = new ArrayList<>();
    private List<Rank> ranking;

    @Before
    public void before() {
        quota = service.createCourse("Computer Science", 500, false, null).
            createAllocationRule(null).createQuota("Performance", 100, null);
        quota.addRankingCriterion("qualification", null);

        List<User> applicants = new ArrayList<>();

        applicants.add(service.createUser("Maurice", "maurice@moss.net",
            "maurice@moss.net:secr3t", User.ROLE_APPLICANT));
        applicants.add(service.createUser("Peter", "peter@pan.com",
            "peter@pan.com:secr3t", User.ROLE_APPLICANT));
        applicants.add(service.createUser("James", "james@hook.net",
            "james@hook.net:secr3t", User.ROLE_APPLICANT));

        Map<String, Object> informationArgs = new HashMap<String, Object>();
        Double grade = 2.0;
        informationArgs.put("grade", grade);
        for (User user : applicants) {
            if (grade.equals(4.0)) {
                grade = 2.0;
            }
            validApplications.add(course.apply(user.getId(), null));
            user.createInformation("qualification", informationArgs, null);
            grade++;
        }

        incompleteApplication = course.apply(service.createUser("Jen", "barber@reynholm.net",
            "barber@reynholm.net:secr3t", User.ROLE_APPLICANT).getId(), null);

        ranking = course.getAllocationRule().getQuota().generateRanking();
    }

    @Test
    public final void testAddRankingCriterion() {
        QualificationCriterion criterion =
            (QualificationCriterion) service.getCriteria().get("qualification");
        quota.addRankingCriterion("qualification", null);
        assertTrue(quota.getRankingCriteria().contains(criterion));
    }

    @Test
    public final void testAddRankingCriterionRedundantCriterionId() {
        QualificationCriterion criterion =
            (QualificationCriterion) service.getCriteria().get("qualification");
        quota.addRankingCriterion("qualification", null);
        quota.addRankingCriterion("qualification", null);
        assertTrue(quota.getRankingCriteria().contains(criterion));
    }

    @Test
    public void testGenerateRanking() {
        for (int index = 0; index < ranking.size() -1; index++) {
            Double evaluationValue = ranking.get(index).getApplication()
                .getEvaluationByCriterionId("qualification").getValue();
            Double nextEvaluationValue = ranking.get(index + 1).getApplication()
                .getEvaluationByCriterionId("qualification").getValue();
            int lotnumber = ranking.get(index).getLotnumber();
            int nextLotnumber =  ranking.get(index + 1).getLotnumber();

            assertTrue(evaluationValue < nextEvaluationValue || lotnumber < nextLotnumber);
        }
    }

    @Test
    public void testGenerateRankingAllValid() {
        assertEquals(validApplications.size(), ranking.size());
    }

    @Test
    public void testGenerateRankingNoIncomplete() {
        for (Rank rank : ranking) {
            assertFalse(rank.getApplication().equals(incompleteApplication));
        }
    }
}