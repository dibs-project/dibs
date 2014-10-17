package de.huberlin.cms.dosv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Random;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.Application;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.HubTest;
import de.huberlin.cms.hub.User;

public class DosvSyncIT extends HubTest {
    private DosvSync dosvSync;

    @Before
    public void before() throws Exception {
        Set<Object> dosvConfigKeys = service.getConfig().keySet();
        assumeTrue(dosvConfigKeys.contains(DosvClient.UNIVERSITY_ID)
            && dosvConfigKeys.contains(DosvClient.USER)
            && dosvConfigKeys.contains(DosvClient.PW));
    }

    @Test
    public void testUserConnectToDosv() {
        String bid = "B189069116325";
        User dosvUser = service.createUser("dosv-testuser", "test@example.org");
        dosvUser.connectToDosv(bid, "603475", null);
        assertEquals(service.getUser(dosvUser.getId()).getDosvBid(), bid);
    }

    @Test
    public void testUserConnectToDosvBadBid() {
        exception.expect(DosvAuthenticationException.class);
        User dosvUser = service.createUser("dosv-testuser", "test@example.org");
        dosvUser.connectToDosv("B189069116325x", "603475", null);
    }

    @Test
    public void testDosvCoursePublish() {
        String randomStr = Integer.toString(new Random().nextInt());
        Course testCourse = service.createCourse("test-" + randomStr, 500, randomStr,
            "test-dosv-degree", null);
        testCourse.createAllocationRule(null).createQuota("Standard", 100, null).
            addRankingCriterion("qualification", null);
        testCourse.publish(null);
        dosvSync = new DosvSync(service);
        dosvSync.synchronize();
        assertTrue(service.getCourse(testCourse.getId()).isDosvPushed());
    }
}
