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
import de.huberlin.cms.hub.HubTest;
import de.huberlin.cms.hub.User;

public class DosvSyncIT extends HubTest {
    private String bid;
    private String ban;

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
        course.unpublish(null);
        service.getDosvSync().synchronize();
        assertTrue(service.getSettings().getDosvSyncTime()
            .after(course.getModificationTime()));
    }

    @Test
    public void testSynchronizeApplications() {
        user.connectToDosv(bid, ban, null);
        Application application = course.apply(user.getId(), null);
        service.getDosvSync().synchronize();
        assertTrue(service.getSettings().getDosvSyncTime()
            .after(application.getModificationTime()));
    }
}
