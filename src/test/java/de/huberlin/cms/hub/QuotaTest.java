/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class QuotaTest extends HubTest {
    private Quota quota;
    private Quota quota1;
    private User user1;
    private User user2;
    private User user3;
    private Application app1;
    private Application app2;
    private Application app3;
    private List<Evaluation> eval1;
    private List<Evaluation> eval2;
    private List<Evaluation> eval3;
    private Information info1;
    private Information info2;
    private Information info3;

    @Before
    public void before() {
        quota = service.createCourse("Computer Science", 500, null).
            createAllocationRule(null).createQuota("Performance", 100, null);
        user1 = this.service.createUser("Maurice", "maurice@moss.net");
        user2 = this.service.createUser("Peter", "peter@pan.com");
        user3 = this.service.createUser("James", "james@hook.net");
        app1 = course.apply(user1.getId(), null);
        app2 = course.apply(user2.getId(), null);
        app3 = course.apply(user3.getId(), null);
        eval1 = app1.getEvaluations(null);
        info1 = new Qualification("1", app1.getUser().getId(), 2, service);
        eval1.get(0).assignInformation(info1);
        eval2 = app2.getEvaluations(null);
        info2 = new Qualification("2", app2.getUser().getId(), 3, service);
        eval2.get(0).assignInformation(info2);
        eval3 = app3.getEvaluations(null);
        info3 = new Qualification("3", app3.getUser().getId(), 2, service);
        eval3.get(0).assignInformation(info3);
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
    public void testGetApplications() {
        HashSet<Application> applications = new HashSet<>();
        applications.add(app1);
        applications.add(app2);
        applications.add(app3);
        quota1 = this.service.getCourse(this.course.getId()).getAllocationRule().getQuota();
        assertEquals(applications, new HashSet<>(quota1.getApplications()));
    }

    @Test
    public void testGetEvaluations() {
        quota1 = this.service.getCourse(this.course.getId()).getAllocationRule().getQuota();
        Map<Application,List<Evaluation>> evaluations = quota1.getEvaluations();
        assertTrue(evaluations.values().contains(app1.getEvaluations(null)));
        assertTrue(evaluations.values().contains(app2.getEvaluations(null)));
    }

    @Test
    public void testGenerateRanking() {
        quota1 = this.service.getCourse(this.course.getId()).getAllocationRule().getQuota();
        List<Rank> ranking = quota1.generateRanking();
        assertTrue(this.service.getApplication(ranking.get(1).getApplication().getId())
                .getEvaluations(null).get(0).getValue() <
                this.service.getApplication(ranking.get(2).getApplication().getId())
                .getEvaluations(null).get(0).getValue());
    }

    @Test
    public void testLotnumber() {
        quota1 = this.service.getCourse(this.course.getId()).getAllocationRule().getQuota();
        List<Rank> ranking = quota1.generateRanking();
        assertTrue(ranking.get(0).getLotnumber() < ranking.get(1).getLotnumber());
    }
}
