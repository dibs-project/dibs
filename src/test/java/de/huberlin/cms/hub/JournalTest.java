package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
    Journal journal;
    ApplicationService app;
    List <JournalRecord> listRecord = new ArrayList<JournalRecord> ();

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
        journal = new Journal();
        journal.db = app.db;
    }

    @Test
    public void testRecord() throws SQLException {
        JournalRecord record;
        try {
            record = journal.record(
                    ActionType.USER_CREATED, ObjectType.APPLICANT, 2, 3, "ABCm34");
            assertNotNull(record);
            assertEquals("ABCm34", record.getDetail());
            assertEquals(ActionType.USER_CREATED, record.getActionType());
            assertEquals(ObjectType.APPLICANT, record.getObjectType());
            assertEquals(2, record.getObjectID());
            assertEquals(3, record.getUserID());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testRecordInvalidParameters() throws SQLException {
        JournalRecord record;
        try {
            record = journal.record(
                    ActionType.USER_CREATED, ObjectType.APPLICANT,0 ,0 , "ABCm34");
            assertNotNull(record);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testGetRecord() throws SQLException {
        JournalRecord record = journal.getRecord(8);
        assertEquals("USER_CREATED",record.actionType.toString());
        assertEquals("ABCm34",record.detail);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetRecordInvalidId() throws SQLException {
        JournalRecord record = journal.getRecord(9999999);
        assertNull(record);
    }

    @Test
    public void testGetJournalperUserID() throws SQLException {
        listRecord = journal.getJournal(2);
        assertEquals(1,listRecord.size());
        assertEquals(ActionType.USER_CREATED,listRecord.get(0).actionType);
        assertEquals("ABCm34",listRecord.get(0).detail);
    }

    @Test
    public void testGetJournalInvalidUserID() throws SQLException {
        listRecord = journal.getJournal(-1);
        assertEquals(0,listRecord.size());
    }

    @Test
    public void testGetObject() throws SQLException {
        listRecord = journal.getJournal(ObjectType.APPLICANT, 78);
        assertEquals(1,listRecord.size());
        assertEquals(ActionType.USER_CREATED,listRecord.get(0).actionType);
        assertEquals("Session_ID 234Bjkllfa",listRecord.get(0).detail);
    }

    @Test
    public void testGetObjectInvalidObjectID() throws SQLException {
        listRecord = journal.getJournal(ObjectType.COURSE, -1);
        assertEquals(0,listRecord.size());
    }

}
