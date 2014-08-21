/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.io.IOError;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.junit.Before;
import org.junit.Test;

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
    public final void testCreateQuotaOutOfRangePercentage() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("percentage");
        rule.createQuota("Performance", -1, null);
    }

    @Test
    public final void testCreateQuotaEmptyName() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("name");
        rule.createQuota("", 100, null);
    }

    @Test
    public void getQuotatest() {
        Quota quota = rule.createQuota("Performance", 100, null);
        Quota quotaErg = this.service.getQuota(quota.getId());
        assertEquals(quota, quotaErg);
        assertEquals(100, quotaErg.getPercentage());
    }
}
