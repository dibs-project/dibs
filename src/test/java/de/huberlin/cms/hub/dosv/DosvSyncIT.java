/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.Application;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.HubTest;
import de.huberlin.cms.hub.User;

public class DosvSyncIT extends HubTest {
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
        assertTrue(service.getSettings().getDosvSyncTime()
            .after(dosvCourse.getModificationTime()));
    }

    @Test
    public void testSynchronizeApplications() {
        user.connectToDosv(bid, ban, null);
        Application application = dosvCourse.apply(user.getId(), null);
        service.getDosvSync().synchronize();
        assertEquals(-1, application.getDosvVersion());
        service.getDosvSync().synchronize();
        assertEquals(0, service.getApplication(application.getId()).getDosvVersion());
        service.getDosvSync().synchronize();
        assertEquals(0, service.getApplication(application.getId()).getDosvVersion());
        assertTrue(service.getSettings().getDosvSyncTime()
            .after(application.getModificationTime()));
    }

    @Test
    public void testSystem() {
        user.connectToDosv(bid, ban, null);
        dosvCourse.apply(user.getId(), null);
        service.getDosvSync().synchronize();
        dosvCourse.startAdmission(null);
        assertTrue(dosvCourse.isAdmission());
        assertTrue(service.getSettings().getDosvSyncTime()
            .before(dosvCourse.getModificationTime()));
        service.getDosvSync().synchronize();
    }
}
