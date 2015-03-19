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
