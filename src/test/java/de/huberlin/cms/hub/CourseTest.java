package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.AllocationRule;
import de.huberlin.cms.hub.Course;

public class CourseTest extends HubTest {
    private Course course;
    private AllocationRule allocation;

    @Before
    public void before() {
        this.course = this.service.createCourse("Computer Science", 500, null);
        this.allocation = this.course.createAllocationRule("Standard", user);
    }

    @Test
    public void testCreateAllocationRule() {
        String name = "Standard";
        AllocationRule allocation = this.course.createAllocationRule(name, user);
        assertEquals(name, allocation.getName());
        assertTrue(this.course.getAllocationRules().contains(allocation));
    }

    @Test
    public void testCreateAllocationRuleEmptyName() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("name");
        course.createAllocationRule("", user);
    }

    @Test
    public void testGetAllocationRule() {
        assertEquals(this.allocation,
            this.course.getAllocationRule(this.allocation.getId()));
    }

    @Test
    public void testGetAllocationRuleNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        course.getAllocationRule("foo");
    }
}
