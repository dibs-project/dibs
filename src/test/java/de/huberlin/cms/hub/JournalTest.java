/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Test der Klasse Journal.
 *
 * @author haphuong
 */

public class JournalTest extends HubTest{
    private static final ActionType ACTION_TYPE = ActionType.USER_CREATED;
    private static final ObjectType OBJECT_TYPE = ObjectType.APPLICANT;
    private static final int OBJECT_ID = 1;
    private static final int USER_ID = 2;
    private static final String DETAIL = "SessionExample";

    protected Journal journal;
    JournalRecord first_record;
    JournalRecord null_record;
    JournalRecord record_empty_action_type;
    JournalRecord empty_detail;

    @Before
    public void before() throws IOException, SQLException {
        super.commonBefore();
        this.journal = new Journal(this.service);
        this.first_record = journal.record(ACTION_TYPE, OBJECT_TYPE, OBJECT_ID, USER_ID,
            DETAIL);
    }

    public boolean isEmptyList(int sizeOfList){
        boolean result = false;
        if(sizeOfList == 0){
            result = true;
        }
        return result;
    }

    @Test
    public void testRecord() {
        assertEquals(ACTION_TYPE, first_record.getActionType());
        assertEquals(OBJECT_TYPE, first_record.getObjectType());
        assertEquals(OBJECT_ID, first_record.getObjectId());
        assertEquals(USER_ID, first_record.getUserId());
        assertEquals(DETAIL, first_record.getDetail());
    }

    @Test
    public void testEmptyObjectTypEmptyDetail() {
        this.journal = new Journal(this.service);
        this.null_record = journal.record(ACTION_TYPE, null, 0, 0, null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRecordNullActionType() {
        this.record_empty_action_type = journal.record(null, OBJECT_TYPE, OBJECT_ID,
            USER_ID, DETAIL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRecordInvalidObjectId() {
        this.record_empty_action_type = journal.record(ACTION_TYPE, OBJECT_TYPE, -1,
            USER_ID, DETAIL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testRecordInvalidUserId() {
        this.record_empty_action_type = journal.record(ACTION_TYPE, OBJECT_TYPE, 0, -1,
            DETAIL);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetRecordInvalidId() throws SQLException {
        journal.getRecord(99999);
    }

    @Test
    public void testGetJournalByUser() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(USER_ID);
        assertFalse(this.isEmptyList(records.size()));
        assertEquals(ACTION_TYPE, records.get(0).getActionType());
        assertEquals(DETAIL, records.get(0).getDetail());
    }

    @Test
    public void testGetJournalByZeroUserId() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(0);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetJournalByInvalidUser() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(-1);
    }

    @Test
    public void testGetJournalByObject() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(OBJECT_TYPE, 1);
        assertFalse(this.isEmptyList(records.size()));
    }

    @Test
    public void testGetRecord() throws SQLException {
        JournalRecord record = journal.getRecord(this.first_record.getId());
        assertEquals(ACTION_TYPE, record.getActionType());
    }
}
