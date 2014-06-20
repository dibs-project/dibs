/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        JournalRecord record =
            journal.record(ActionType.USER_CREATED, null, null, null, null);
        assertEquals(ActionType.USER_CREATED, record.getActionType());
    }

    @Test
    public void testGetRecord() {
        JournalRecord record =
            journal.record(ActionType.USER_CREATED, null, null, null, null);
        assertEquals(record, journal.getRecord(record.getId()));
    }

    @Test
    public void testGetRecordNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        journal.getRecord("foo");
    }

    @Test
    public void testGetRecordsUser() {
        JournalRecord record =
            journal.record(ActionType.USER_CREATED, null, null, null, null);
        List<JournalRecord> records = journal.getRecords(null);
        assertTrue(records.contains(record));
        for (JournalRecord r : records) {
            assertEquals(null, r.getUserId());
        }
    }

    @Test
    public void testGetRecordsObjectNonNullObjectId() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("objectId");
        journal.getRecords(null, "foo");
    }

    @Test
    public void testGetRecordsObjectNullObjectId() {
        this.exception.expect(NullPointerException.class);
        this.exception.expectMessage("objectId");
        journal.getRecords(ObjectType.USER, null);
    }
}
