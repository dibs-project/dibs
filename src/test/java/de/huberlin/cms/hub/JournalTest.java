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

public class JournalTest extends HubTest{
    static final int FIRST_RECORD_ID = 1;
    static final ActionType ACTION_TYPE = ActionType.USER_CREATED;
    static final ObjectType OBJECT_TYPE = ObjectType.APPLICANT;
    static final int OBJECT_ID = 1;
    static final int USER_ID = 2;
    static final String DETAIL = "SessionExample";

    Journal journal;
    JournalRecord first_record;

    @Before
    public void before() throws IOException, SQLException {
        super.before();
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
    public void testGetRecord() throws SQLException {
        JournalRecord record = journal.getRecord(this.first_record.getId());
        assertEquals(ACTION_TYPE, record.actionType);
        assertEquals(DETAIL, record.detail);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetRecordInvalidId() throws SQLException {
        journal.getRecord(9999999);
    }

    @Test
    public void testGetJournalByUser() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(USER_ID);
        assertFalse(this.isEmptyList(records.size()));
        assertEquals(ACTION_TYPE, records.get(0).actionType);
        assertEquals(DETAIL, records.get(0).detail);
    }

    @Test
    public void testGetJournalByObject() throws SQLException {
        List <JournalRecord> records = new ArrayList<JournalRecord>();
        records = journal.getJournal(OBJECT_TYPE, 1);
        assertFalse(this.isEmptyList(records.size()));
    }
}
