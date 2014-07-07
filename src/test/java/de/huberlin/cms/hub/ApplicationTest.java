package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Michler
 */
public class ApplicationTest extends HubTest {
    private Application application;

    @Before
    public void before() throws Exception {
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