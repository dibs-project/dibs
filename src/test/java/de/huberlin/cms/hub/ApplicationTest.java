package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ApplicationTest extends HubTest {
    private Course course;
    private Application application;

    @Before
    public void before() throws Exception {
        course = service.createCourse("Computer Science", 500, null);
        application = course.apply(user.getId(), user);
    }

    @Test
    public final void testSetStatus() {
        String newStatus = Application.STATUS_COMPLETE;
        application.setStatus(newStatus, null);
        application = service.getApplication(application.getId());
        assertEquals(application.getStatus(), newStatus);
    }
}