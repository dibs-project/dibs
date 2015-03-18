/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.AllocationRule;
import university.dibs.dibs.Quota;
import university.dibs.dibs.DibsException.IllegalStateException;

public class AllocationRuleTest extends DibsTest {
    private AllocationRule rule;

    @Before
    public void before() {
        rule = course.getAllocationRule();
        course.unpublish(null);
    }

    @Test
    public final void testCreateQuota() {
        Quota quota = rule.createQuota("Performance", 100, null);
        assertEquals(quota, rule.getQuota());
    }

    @Test
    public void testCreateQuotaPublished() {
        exception.expect(IllegalStateException.class);
        course.publish(null);
        course.getAllocationRule().createQuota("Performance", 100, null);
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
}
