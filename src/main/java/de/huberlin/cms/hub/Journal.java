package de.huberlin.cms.hub;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;


public class Journal {
    public List<JournalRecord> journal = new ArrayList <JournalRecord>();
    ApplicationService app;
    Connection db;

    /**
     * Schreiben einen Eintrag im Protokollbuch
     *
     * @param actionType Typ der hub-Aktion
     * @param objectType Typ des hub-Objekt
     * @param objectID ID des Objekts
     * @param userID ID des Sachbearbeiters
     * @param detail  Beschreibung des Eintrags
     * @return JournalRecord der Eintrag
     * @throws IOException
     * @throws SQLException
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            int objectID, int userID, String detail) throws IOException, SQLException {
        try {
            String SQL =
                "INSERT INTO dosv.record ("
                + "action_type, object_type, object_id, user_id,timestamp, detail) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement =
                this.db.prepareStatement(SQL, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, actionType.toString());
            statement.setString(2, objectType.toString());
            statement.setInt(3, objectID);
            statement.setInt(4, userID);
            java.util.Date date = new java.util.Date();
            Timestamp timestamp = new Timestamp(date.getTime());
            statement.setTimestamp(5, timestamp);
            statement.setString(6, detail);
            statement.executeUpdate();

            ResultSet results = statement.getGeneratedKeys();
            if (results.next()) {
                int record_id = results.getInt("record_id");
                JournalRecord record = getRecord(record_id);
                db.commit();
                return record;
            }else {
                throw new IllegalArgumentException("invalid record ID");
            }
        } catch (SQLException e) {
            this.db.rollback();
            throw new IOException(e);
        }
    }

    /**
     * Gibt den Eintrag mit der spezifizierten ID zurück.
     *
     * @param record_id ID des Eintrags
     * @return JournalRecord der Eintrag
     * @throws SQLException 
     */
    public JournalRecord getRecord(int record_id) throws SQLException {
        PreparedStatement statement =
            db.prepareStatement("SELECT * FROM dosv.record WHERE record_id=?");
        statement.setInt(1, record_id);
        ResultSet results = statement.executeQuery();
        if (!results.next()) {
            throw new IllegalArgumentException("invalid record ID");
        }
        return new JournalRecord(results);
    }

    /**
     * Gibt alle Einträge mit der spezifizierten Type und ID eines Objekt zurück.
     *
     * @param objectTypeID ID des Objekt-Typ
     * @param objectID ID des Objekts
     * @return List<JournalRecord> Liste der Einträgen
     * @throws SQLException 
     */
    public List<JournalRecord> getJournal(ObjectType objectType, int objectID) throws
            SQLException {
        PreparedStatement statement =
            db.prepareStatement("SELECT * FROM dosv.record WHERE object_type=? AND "
                + "object_id=?");
        statement.setString(1, objectType.toString());
        statement.setInt(2, objectID);
        ResultSet results = statement.executeQuery();
        while(results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }

    /**
     * Gibt alle Einträge mit der Sachbearbeiter-ID zurück.
     *
     * @param userID ID des Sachbearbeiters
     * @return List<JournalRecord> Liste der Einträgen
     * @throws SQLException 
     */
    public List<JournalRecord> getJournal(int userID) throws SQLException {
        PreparedStatement statement =
            db.prepareStatement("SELECT * FROM dosv.record WHERE user_id=?");
        statement.setInt(1, userID);
        ResultSet results = statement.executeQuery();
        while(results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }
}
