/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import university.dibs.dibs.ApplicationService;
import university.dibs.dibs.Journal;
import university.dibs.dibs.JournalRecord;
import university.dibs.dibs.HubException.ObjectNotFoundException;

public class JournalTest extends HubTest {
    private Journal journal;

    @Before
    public void before() {
        this.journal = this.service.getJournal();
    }

    @Test
    public void testRecord() {
        JournalRecord record =
            journal.record(ApplicationService.ACTION_TYPE_USER_CREATED, null, null, null);
        assertEquals(ApplicationService.ACTION_TYPE_USER_CREATED, record.getActionType());
    }

    @Test
    public void testGetRecord() {
        JournalRecord record =
            journal.record(ApplicationService.ACTION_TYPE_USER_CREATED, null, null, null);
        assertEquals(record, journal.getRecord(record.getId()));
    }

    @Test
    public void testGetRecordNonExisting() {
        this.exception.expect(ObjectNotFoundException.class);
        journal.getRecord("foo");
    }

    @Test
    public void testGetRecordsByAgentId() {
        JournalRecord record =
            journal.record(ApplicationService.ACTION_TYPE_USER_CREATED, null, this.user.getId(), null);
        List<JournalRecord> records = journal.getRecordsByAgentId(this.user.getId());
        assertTrue(records.contains(record));
        for (JournalRecord r : records) {
            assertEquals(this.user, r.getAgent());
        }
    }

    @Test
    public void testGetRecordsByObjectId() {
        String objectId = "application:1";
        JournalRecord record =
            journal.record(ApplicationService.ACTION_TYPE_APPLICATION_STATUS_SET, objectId, null, null);
        List<JournalRecord> records = journal.getRecordsByObjectId(objectId);
        assertTrue(records.contains(record));
        for (JournalRecord r : records) {
            assertEquals(objectId, r.getObjectId());
        }
    }
}
