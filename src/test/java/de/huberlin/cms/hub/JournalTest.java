/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * @author Phuong Anh Ha
 */
public class JournalTest extends HubTest {
    private Journal journal;

    @Before
    public void before() {
        this.journal = this.service.getJournal();
    }

    @Test
    public void testRecord() {
        JournalRecord record = journal.record(ActionType.USER_CREATED, null, null, null, null);
        assertEquals(ActionType.USER_CREATED, record.getActionType());
    }

    @Test
    public void testGetRecord() {
        JournalRecord record = journal.record(ActionType.USER_CREATED, null, null, null, null);
        JournalRecord testRecord = journal.getRecord(record.getId());
        assertEquals(record.getId(), testRecord.getId());
    }

    @Test
    public void testGetRecordNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        journal.getRecord("foo");
    }

    @Test
    public void testGetJournalUser() {
        journal.record(ActionType.USER_CREATED, null, null, this.user.getId(), null);
        List<JournalRecord> records = journal.getRecords(this.user.getId());
        assertEquals(this.user.getId(), records.get(0).getUserId());
    }

    @Test
    public void testGetJournalObjectNonNullObjectId() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("objectId");
        journal.getRecords(null, "foo");
    }

    @Test
    public void testGetJournalObjectNullObjectId() {
        this.exception.expect(NullPointerException.class);
        this.exception.expectMessage("objectId");
        journal.getRecords(ObjectType.USER, null);
    }
}
