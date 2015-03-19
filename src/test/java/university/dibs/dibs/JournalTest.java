/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
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
import university.dibs.dibs.DibsException.ObjectNotFoundException;

public class JournalTest extends DibsTest {
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
