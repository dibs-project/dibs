package de.huberlin.cms.hub;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest extends HubTest {
    private Course course;
    Application application;

    @Before
    public void before() throws Exception {
        course = service.createCourse("Computer Science", 500, this.user);
        application = course.apply(user.getId(), user);
    }

    @Test
    public final void testSetStatus() {
        application.setStatus(Application.STATUS_COMPLETE, user);
        application = user.getApplications(user).get(0);
        assertTrue(application.getStatus().equals(Application.STATUS_COMPLETE));
    }

}
