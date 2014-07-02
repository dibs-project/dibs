package de.huberlin.cms.hub;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CourseTest extends HubTest {
    private Course course;

    @Before
    public void before() {
        course = service.createCourse("Computer Science", 500, this.user);
    }

    @Test
    public final void testApply() {
        Application application = course.apply(user.getId(), null);
        assertTrue(user.getApplications(null).contains(application));
    }
}
