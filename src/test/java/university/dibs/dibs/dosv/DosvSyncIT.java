/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs.dosv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.Course;
import university.dibs.dibs.DibsTest;
import university.dibs.dibs.User;
import de.hu_berlin.dosv.DosvClient;

public class DosvSyncIT extends DibsTest {
    private String bid;
    private String ban;
    private Course dosvCourse;

    @Before
    public void before() throws Exception {
        Properties config = service.getConfig();
        /** Überspringt den Integration Test, wenn der DoSV-Webservice nicht konfiguriert ist */
        assumeTrue(!config.getProperty(DosvClient.UNIVERSITY_ID).isEmpty()
            && !config.getProperty(DosvClient.USER).isEmpty()
            && !config.getProperty(DosvClient.PW).isEmpty()
            && !config.getProperty("dosv_test_bid").isEmpty()
            && !config.getProperty("dosv_test_ban").isEmpty());
        bid = config.getProperty("dosv_test_bid");
        ban = config.getProperty("dosv_test_ban");
        dosvCourse = service.createCourse("Computer Science", 500, true, null);
        dosvCourse.createAllocationRule(null).createQuota("performance", 100, null)
            .addRankingCriterion("qualification", null);
        dosvCourse.publish(null);
    }

    @Test
    public void testUserConnectToDosv() {
        User dosvUser = service.createUser("dosv-testuser", "test@example.org",
            "test@example.org:secr3t", User.ROLE_APPLICANT);
        assertTrue(dosvUser.connectToDosv(bid, ban, null));
        assertEquals(bid, service.getUser(dosvUser.getId()).getDosvBid());
    }

    @Test
    public void testUserConnectToDosvBadBid() {
        User dosvUser = service.createUser("dosv-testuser", "test@example.org",
            "test@example.org:secr3t", User.ROLE_APPLICANT);
        assertFalse(dosvUser.connectToDosv("BadBid", ban, null));
    }

    @Test
    public void testSynchronizeCourseModified() {
        service.getDosvSync().synchronize();
        dosvCourse.unpublish(null);
        service.getDosvSync().synchronize();
    }

    @Test
    public void testSynchronizeApplications() {
        user.connectToDosv(bid, ban, null);
        String applicationId = dosvCourse.apply(user.getId(), null).getId();
        service.getDosvSync().synchronize();
        assertEquals(-1, service.getApplication(applicationId).getDosvVersion());
        service.getDosvSync().synchronize();
        assertEquals(0, service.getApplication(applicationId).getDosvVersion());
        service.getDosvSync().synchronize();
        assertEquals(0, service.getApplication(applicationId).getDosvVersion());
    }

    @Test
    public void testSystem() {
        user.connectToDosv(bid, ban, null);
        dosvCourse.apply(user.getId(), null);
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        user.createInformation("qualification", args, null);
        service.getDosvSync().synchronize();
        dosvCourse.startAdmission(null);
        service.getDosvSync().synchronize();
    }
}
