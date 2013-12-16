package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

public class JournalTest {
    static final int FIRST_RECORD_ID = 1;
    static final ActionType ACTION_TYPE = ActionType.USER_CREATED;
    static final ObjectType OBJECT_TYPE = ObjectType.APPLICANT;
    static final int OBJECT_ID = 1;
    static final int USER_ID = 2;
    static final String DETAIL = "SessionExample";

    Journal journal;
    ApplicationService app;

    @Before
    public void before() throws IOException, SQLException {
        Properties config;
        config = new Properties();
        try {
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // skip the tests
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        app = new ApplicationService(ApplicationService.openDatabase(config), config);
        journal = new Journal(app);
    }

    public boolean isEmptyList(int sizeOfList){
        boolean result = false;
        if(sizeOfList == 0){
            result = true;
        }
        return result;
    }

    @Test
    public void testRecord() throws IOException, SQLException {
        JournalRecord record;
        record = journal.record(
                ACTION_TYPE, OBJECT_TYPE, OBJECT_ID, USER_ID, DETAIL);
        assertEquals(ACTION_TYPE, record.getActionType());
        assertEquals(OBJECT_TYPE, record.getObjectType());
        assertEquals(OBJECT_ID, record.getObjectId());
        assertEquals(USER_ID, record.getUserId());
        assertEquals(DETAIL, record.getDetail());
    }

    @Test
    public void testGetRecord() throws SQLException {
        JournalRecord record = journal.getRecord(FIRST_RECORD_ID);
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
