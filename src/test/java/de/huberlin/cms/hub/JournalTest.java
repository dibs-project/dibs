/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * @author Phuong Anh Ha
 */
public class JournalTest extends HubTest {
    Journal journal;
    JournalRecord record;

    @Before
    public void before() throws IOException, SQLException {
        this.journal = this.service.getJournal();
    }

    @Test
    public void testRecord() throws SQLException {
        record = this.journal.record(ActionType.USER_CREATED, null, null, null, null);
        assertTrue(record.getActionType().equals(ActionType.USER_CREATED));
    }

    @Test
    public void testGetRecord() {
        record = this.journal.record(ActionType.USER_CREATED, null, null, null, null);
        JournalRecord testRecord = this.journal.getRecord(record.getId());
        assertEquals(record.getId(), testRecord.getId());
    }

    @Test
    public void testGetRecordNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        journal.getRecord("foo");
    }

    @Test
    public void testGetJournalUser() throws SQLException {
        this.journal.record(ActionType.USER_CREATED, null, null, this.user.getId(), null);
        List<JournalRecord> records = journal.getJournal(this.user.getId());
        assertEquals(this.user.getId(), records.get(0).getUserId());
    }

    @Test
    public void testGetJournalObjectIllegalObjectId() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("objectId");
        journal.getJournal(null, "foo");
    }

    @Test
    public void testGetJournalObjectNullPointerObjectId() {
        this.exception.expect(NullPointerException.class);
        this.exception.expectMessage("objectId");
        journal.getJournal(ObjectType.USER, null);
    }
}
