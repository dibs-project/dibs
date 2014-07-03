package de.huberlin.cms.hub;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Markus Michler
 */
public class CourseTest extends HubTest {
    @Test
    public final void testApply() {
        Application application = course.apply(user.getId(), null);
        assertTrue(user.getApplications(null).contains(application));
    }
}