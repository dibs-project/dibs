/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class QuotaTest extends HubTest {
    private Quota quota;

    @Before
    public void before() {
        quota = service.createCourse("Computer Science", 500, "cs", "bsc", null).
            createAllocationRule(null).createQuota("Performance", 100, null);
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
}
