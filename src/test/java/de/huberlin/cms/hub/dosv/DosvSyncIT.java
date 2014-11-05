/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assume.assumeTrue;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.HubTest;
import de.huberlin.cms.hub.User;

public class DosvSyncIT extends HubTest {
    private String bid;
    private String ban;

    @Before
    public void before() throws Exception {
        Properties config = service.getConfig();
        /** Überspringt den Integration Test, wenn der DoSV-Webservice nicht konfiguriert ist */
        assumeTrue(!config.get(DosvClient.UNIVERSITY_ID).equals("")
            && !config.get(DosvClient.USER).equals("")
            && !config.get(DosvClient.PW).equals("")
            && !config.get("dosv_test_bid").equals("")
            && !config.get("dosv_test_ban").equals(""));
        bid = config.getProperty("dosv_test_bid");
        ban = config.getProperty("dosv_test_ban");
    }

    @Test
    public void testUserConnectToDosv() {
        User dosvUser = service.createUser("dosv-testuser", "test@example.org");
        dosvUser.connectToDosv(bid, ban, null);
        assertEquals(bid, service.getUser(dosvUser.getId()).getDosvBid());
    }

    @Test
    public void testUserConnectToDosvBadBid() {
        User dosvUser = service.createUser("dosv-testuser", "test@example.org");
        assertFalse(dosvUser.connectToDosv("BadBid", ban, null));
    }
}
