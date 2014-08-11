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
            journal.record(JournalRecord.TYPE_USER_CREATED, null, null, null);
        assertEquals(JournalRecord.TYPE_USER_CREATED, record.getActionType());
    }

    @Test
    public void testGetRecord() {
        JournalRecord record =
            journal.record(JournalRecord.TYPE_USER_CREATED, null, null, null);
        assertEquals(record, journal.getRecord(record.getId()));
    }

    @Test
    public void testGetRecordNonExisting() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("id");
        journal.getRecord("foo");
    }

    @Test
    public void testGetRecordsAgent() {
        JournalRecord record =
            journal.record(JournalRecord.TYPE_USER_CREATED, null, this.user.getId(), null);
        List<JournalRecord> records = journal.getRecordsAgent(this.user.getId());
        assertTrue(records.contains(record));
        for (JournalRecord r : records) {
            assertEquals(this.user.getId(), r.getAgentId());
        }
    }

    @Test
    public void testGetRecordsObject() {
        JournalRecord record =
            journal.record(JournalRecord.TYPE_APPLICATION_STATUS_SET, "application: 1", null, null);
        List<JournalRecord> records = journal.getRecordsObject("application: 1");
        assertTrue(records.contains(record));
        for (JournalRecord r : records) {
            assertEquals("application: 1", r.getObjectId());
        }
    }
}
