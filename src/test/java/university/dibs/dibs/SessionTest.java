/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.Session;

public class SessionTest extends HubTest {
    private Session session;

    @Before
    public void before() {
        this.session = this.service.login(this.user.getCredential(), "localhost");
    }

    @Test
    public void testEnd() {
        Date endTime = this.session.getEndTime();
        this.session.end();
        assertFalse(this.session.isValid());
        assertTrue(this.session.getEndTime().before(endTime));
    }

    @Test
    public void testEndExpired() {
        this.session.end();
        Date endTime = this.session.getEndTime();
        this.session.end();
        assertFalse(this.session.isValid());
        assertEquals(endTime, this.session.getEndTime());
    }
}
