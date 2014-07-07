/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Michler
 */
public class QuotaTest extends HubTest {
    private Quota quota;

    @Before
    public void before() {
        quota = course.createAllocationRule(null).createQuota("Performance", 100, null);
    }

    @Test
    public final void testAddRankingCriterion() {
        QualificationCriterion criterion =
            (QualificationCriterion) quota.addRankingCriterion("qualification", null);
        assertTrue(quota.getRankingCriteria().contains(criterion));
    }
}
