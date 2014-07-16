/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Michler
 */
public class AllocationRuleTest extends HubTest {
    private AllocationRule rule;

    @Before
    public void before() {
        rule = course.getAllocationRule();
    }

    @Test
    public final void testCreateQuota() {
        Quota quota = rule.createQuota("Performance", 100, null);
        assertEquals(quota, rule.getQuota());
    }

    @Test
    public final void testCreateQuotaPercentageLowerConstraint() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("percentage");
        rule.createQuota("Performance", -1, null);
    }

    @Test
    public final void testCreateQuotaPercentageUpperConstraint() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("percentage");
        rule.createQuota("Performance", 101, null);
    }

    @Test
    public final void testCreateQuotaNameNull() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("name");
        rule.createQuota(null, 100, null);
    }

    @Test
    public final void testCreateQuotaEmptyName() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("name");
        rule.createQuota("", 100, null);
    }
}
