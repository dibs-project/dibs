package de.huberlin.cms.hub;

import java.io.IOError;
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
    ApplicationService service;
    Connection db;

    public Journal(ApplicationService service) {
        this.service = service;
        this.db = this.service.db;
    }

    /**
     * Schreibt einen Eintrag ins Logbuch.
     *
     * @param actionType Typ der Aktion
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @param userId ID des Nutzers
     * @param detail  Beschreibung des Eintrags
     * @return JournalRecord der Eintrag
     * @throws IOException
     * @throws SQLException
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            int objectId, int userId, String detail) {
        try {
            // TODO: Parameter überprüfen
            String SQL =
                "INSERT INTO dosv.journal_record ("
                + "action_type, object_type, object_id, user_id, detail, time) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement statement =
                service.db.prepareStatement(SQL, PreparedStatement.RETURN_GENERATED_KEYS);
            statement.setString(1, actionType.toString());
            statement.setString(2, objectType.toString());
            statement.setInt(3, objectId);
            statement.setInt(4, userId);
            statement.setString(5, detail);
            java.util.Date date = new java.util.Date();
            Timestamp time = new Timestamp(date.getTime());
            statement.setTimestamp(6, time);
            statement.executeUpdate();

            ResultSet results = statement.getGeneratedKeys();
            results.next();
            int id = results.getInt("id");
            JournalRecord record = getRecord(id);
            this.db.commit();
            return record;
        } catch (SQLException e) {
            throw new IOError(e);
        }

    }

    /**
     * Gibt den Eintrag mit der spezifizierten ID zurück.
     *
     * @param id ID des Eintrages
     * @return JournalRecord der Eintrag
     * @throws SQLException
     */
    public JournalRecord getRecord(int id) throws SQLException {
        PreparedStatement statement =
            this.db.prepareStatement("SELECT * FROM dosv.journal_record WHERE id=?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        if (!results.next()) {
            throw new IllegalArgumentException("invalid ID");
        }
        return new JournalRecord(results);
    }

    /**
     * Gibt alle Einträge mit dem spezifizierten Typ und der ID eines Objekts zurück.
     *
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @return List<JournalRecord> Liste der Einträgen
     * @throws SQLException
     */
    public List<JournalRecord> getJournal(ObjectType objectType, int objectId) throws
            SQLException {
        PreparedStatement statement =
            this.db.prepareStatement("SELECT * FROM dosv.journal_record WHERE "
                    + "object_type=? AND object_id=?");
        statement.setString(1, objectType.toString());
        statement.setInt(2, objectId);
        ResultSet results = statement.executeQuery();
        while(results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }

    /**
     * Gibt alle Einträge mit der Nutzer-ID zurück.
     *
     * @param userId ID des Nutzers
     * @return List<JournalRecord> Liste der Einträgen
     * @throws SQLException
     */
    public List<JournalRecord> getJournal(int userId) throws SQLException {
        PreparedStatement statement =
            this.db.prepareStatement("SELECT * FROM dosv.journal_record WHERE user_id=?");
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();
        while(results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }
}
