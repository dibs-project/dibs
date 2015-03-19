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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.Application;
import university.dibs.dibs.QualificationCriterion;
import university.dibs.dibs.Quota;
import university.dibs.dibs.Rank;
import university.dibs.dibs.User;

public class QuotaTest extends DibsTest {
    private Quota quota;
    private List<Application> validApplications = new ArrayList<>();
    private List<Rank> ranking;

    @Before
    public void before() {
        quota = service.createCourse("Computer Science", 500, false, null).
            createAllocationRule(null).createQuota("Performance", 100, null);

        List<User> applicants = new ArrayList<>();

        applicants.add(service.createUser("Maurice", "moss@reynholm.net",
            "moss@reynholm.net:secr3t", User.ROLE_APPLICANT));
        applicants.add(service.createUser("Roy", "trenneman@reynholm.net",
            "trenneman@reynholm.net:secr3t", User.ROLE_APPLICANT));
        applicants.add(service.createUser("Jen", "barber@reynholm.net",
            "barber@reynholm.net:secr3t", User.ROLE_APPLICANT));

        double[] grades = {2, 2, 3};
        for (User user : applicants) {
            validApplications.add(course.apply(user.getId(), null));
            Map<String, Object> informationArgs = new HashMap<String, Object>();
            informationArgs.put("grade", grades[applicants.indexOf(user)]);
            user.createInformation("qualification", informationArgs, null);
        }

        // create incomplete application
        course.apply(user.getId(), null);
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
        ranking = course.getAllocationRule().getQuota().generateRanking();
        for (int index = 0; index < ranking.size() - 1; index++) {
            Double evaluationValue = ranking.get(index).getApplication()
                .getEvaluationByCriterionId("qualification").getValue();
            Double nextEvaluationValue = ranking.get(index + 1).getApplication()
                .getEvaluationByCriterionId("qualification").getValue();

            assertTrue(evaluationValue <= nextEvaluationValue);
        }
        // all ranked applications valid?
        HashSet<Application> rankedApplications = new HashSet<>();
        for (Rank rank : ranking) {
            rankedApplications.add(rank.getApplication());
        }
        assertEquals(new HashSet<Application>(validApplications), rankedApplications);
    }
}