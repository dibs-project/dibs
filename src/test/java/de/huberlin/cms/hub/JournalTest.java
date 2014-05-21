/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
        this.journal = new Journal(this.service);
        this.record = journal.record(ActionType.USER_CREATED, ObjectType.USER, "1",
            "1", "first test");
    }

    @Test
    public void testRecord() {
        assertEquals(ActionType.USER_CREATED, record.getActionType());
    }

    @Test
    public void testRecordNullActionType() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("actionType");
        journal.record(null, ObjectType.USER, "1", "1", "first test");
    }

    @Test
    public void testRecordEmptyUserID() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("userID");
        journal.record(ActionType.USER_CREATED, ObjectType.USER, "1", "", "test");
    }

    @Test
    public void testRecordNullActionTypeEmptyUserID() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("actionType");
        journal.record(null, ObjectType.USER, "1", "", "test");
    }

//    @Test
//    public void testGetRecord() throws SQLException {
//        JournalRecord record = journal.getRecord(record.getId());
//        assertEquals(record.getId(), record.getId());
//    }

    @Test
    public void testGetRecordEmptyID() throws SQLException {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("recordID");
        journal.getRecord("");
    }

    @Test
    public void testGetRecordInvalidID() throws SQLException {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("recordID");
        journal.getRecord("foo");
    }

    @Test
    public void testGetJournal() throws SQLException {
        List<JournalRecord> records = new ArrayList <JournalRecord>();
        records = journal.getJournal(record.getUserId());
        assertEquals(records.get(0).getId(),record.getId() );
    }

    @Test
    public void testGetJournalInvalidUserID() throws SQLException {
        journal.getJournal("foo");
    }

    @Test
    public void testGetJournalEmptyUserID() throws SQLException {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("userID");
        journal.getJournal("");
    }

    @Test
    public void testGetJournalObjectTypeObjectID() throws SQLException {
        List<JournalRecord> records = new ArrayList <JournalRecord>();
        records = journal.getJournal(record.getObjectType(),
            record.getObjectId());
        assertEquals(records.get(0).getId(),record.getId() );
    }

    @Test
    public void testGetJournalNullObjectTypeEmptyObjectID() throws SQLException {
        journal.getJournal(null,"");
    }


}
