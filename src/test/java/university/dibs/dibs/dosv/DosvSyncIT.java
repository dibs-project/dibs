/*
 * dibs
 * Copyright (C) 2015  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package university.dibs.dibs.dosv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import university.dibs.dibs.Course;
import university.dibs.dibs.DibsTest;
import university.dibs.dibs.User;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

public class DosvSyncIT extends DibsTest {
    private static Logger logger = Logger.getLogger(DosvSyncIT.class.getPackage().getName());

    private String bid;
    private String ban;
    private Course dosvCourse;

    @Before
    public void before() throws Exception {
        Properties config = service.getConfig();
        if (this.service.getDosvSync() == null) {
            logger.warning("DosvSync not enabled, skipping integration test");
            assumeTrue(false);
        }
        this.bid = config.getProperty("dosv_test_bid");
        this.ban = config.getProperty("dosv_test_ban");
        this.dosvCourse = this.service.createCourse("Computer Science", 500, true, null);
        this.dosvCourse.createAllocationRule(null).createQuota("performance", 100, null)
            .addRankingCriterion("qualification", null);
        this.dosvCourse.publish(null);
    }

    @Test
    public void testUserConnectToDosv() {
        User dosvUser = this.service.createUser("dosv-testuser", "test@example.org",
            "test@example.org:secr3t", User.ROLE_APPLICANT);
        assertTrue(dosvUser.connectToDosv(this.bid, this.ban, null));
        assertEquals(this.bid, this.service.getUser(dosvUser.getId()).getDosvBid());
    }

    @Test
    public void testUserConnectToDosvBadBid() {
        User dosvUser = this.service.createUser("dosv-testuser", "test@example.org",
            "test@example.org:secr3t", User.ROLE_APPLICANT);
        assertFalse(dosvUser.connectToDosv("BadBid", this.ban, null));
    }

    @Test
    public void testSynchronizeCourseModified() {
        this.service.getDosvSync().synchronize();
        this.dosvCourse.unpublish(null);
        this.service.getDosvSync().synchronize();
    }

    @Test
    public void testSynchronizeApplications() {
        this.user.connectToDosv(this.bid, this.ban, null);
        String applicationId = this.dosvCourse.apply(this.user.getId(), null).getId();
        this.service.getDosvSync().synchronize();
        assertEquals(-1, this.service.getApplication(applicationId).getDosvVersion());

        this.service.getDosvSync().synchronize();
        int versionDosvInitial = this.service.getApplication(applicationId).getDosvVersion();
        this.service.getDosvSync().synchronize();
        int versionDosvNew = this.service.getApplication(applicationId).getDosvVersion();
        assertEquals(versionDosvInitial, versionDosvNew);

        Map<String, Object> args = new HashMap<>();
        args.put("grade", 4.0);
        this.user.createInformation("qualification", args, null);
        this.service.getDosvSync().synchronize();
        this.service.getDosvSync().synchronize();
        assertTrue(versionDosvNew < this.service.getApplication(applicationId).getDosvVersion());
    }

    @Test
    public void testSystem() {
        this.user.connectToDosv(this.bid, this.ban, null);
        this.dosvCourse.apply(this.user.getId(), null);
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("grade", 4.0);
        this.user.createInformation("qualification", args, null);
        this.service.getDosvSync().synchronize();
        this.dosvCourse.startAdmission(null);
        this.service.getDosvSync().synchronize();
    }
}
