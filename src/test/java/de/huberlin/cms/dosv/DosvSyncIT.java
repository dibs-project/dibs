/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.dosv;

import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.IN_VORBEREITUNG;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.OEFFENTLICH_SICHTBAR;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import de.hochschulstart.hochschulschnittstelle.studiengaengeservicev1_0.StudiengaengeServiceFehler;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Studienangebot;
import de.hu_berlin.dosv.DosvClient;
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
    public void testCoursePushUnpublished() throws StudiengaengeServiceFehler {
        course.unpublish(null);
        dosvSync = new DosvSync(service);
        dosvSync.synchronize();
        assertTrue(service.getCourse(course.getId()).isDosvPushed());
        DosvClient client = new DosvClient(config);
        List<Studienangebot> angebote =
            client.abrufenStudienangeboteDurchHS(course.getDosvDegreeKey());
        for (Studienangebot angebot : angebote) {
            if (angebot.getNameDe().equals(course.getName())) {
                if (!angebot.getStatus().equals(IN_VORBEREITUNG)) {
                    fail("DoSV-Status of unpublished course has to be 'In Vorbereitung'");
                }
            }
        }
    }

    @Test
    public void testCoursePushPublished() throws StudiengaengeServiceFehler {
        dosvSync = new DosvSync(service);
        dosvSync.synchronize();
        assertTrue(service.getCourse(course.getId()).isDosvPushed());
        DosvClient client = new DosvClient(config);
        List<Studienangebot> angebote =
            client.abrufenStudienangeboteDurchHS(course.getDosvDegreeKey());
        for (Studienangebot angebot : angebote) {
            if (angebot.getNameDe().equals(course.getName())) {
                if (!angebot.getStatus().equals(OEFFENTLICH_SICHTBAR)) {
                    fail("DoSV-Status of published course has to be 'Öffentlich Sichtbar'");
                }
            }
        }
    }
}
