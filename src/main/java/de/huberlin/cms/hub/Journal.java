/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

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

/**
 * Journal Klasse - Protokollbuch, in welchem sämtliche prozessrelevanten Aktionen
 * protokolliert werden.
 *
 * @author haphuong
 */
public class Journal {
    public List<JournalRecord> journal = new ArrayList <JournalRecord>();
    ApplicationService service;
    Connection db;

    public Journal(ApplicationService service) {
        this.service = service;
        this.db = this.service.db;
    }

    /**
     * Schreibt einen Protokolleintrag in das Protokoll.
     *
     * @param actionType Typ der Aktion, darf nicht null sein.
     * @param objectType Typ des Objekts.
     * @param objectId ID des Objekts.
     * @param userId ID des Nutzers.
     * @param detail  Beschreibung des Eintrags.
     * @return der Eintrag.
     * @throws IOException
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            int objectId, int userId, String detail) {
        java.util.Date date = new java.util.Date();
        Timestamp time = new Timestamp(date.getTime());

        if (actionType == null || time == null || objectId < 0 || userId < 0){
            throw new IllegalArgumentException("invalid Action Type");
        } else {
            try {
                String sql =
                    "INSERT INTO dosv.journal_record ("
                    + "action_type, object_type, object_id, user_id, detail, time) "
                    + "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

                PreparedStatement statement = service.db.prepareStatement(sql);
                statement.setString(1, actionType.toString());
                statement.setString(2, objectType == null ? null : objectType.toString());
                statement.setInt(3, objectId);
                statement.setInt(4, userId);
                statement.setString(5, detail);
                statement.setTimestamp(6, time);
                ResultSet results = statement.executeQuery();
                results.next();
                int id = results.getInt("id");
                JournalRecord record = getRecord(id);
                this.db.commit();
                return record;
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }

    /**
     * Gibt den Protokolleintrag mit der spezifizierten ID zurück.
     *
     * @param id ID des Protokolleintrag.
     * @return den Protokollseintrag mit der spezifizierten ID.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public JournalRecord getRecord(int id) throws SQLException {
        if (id < 0) {
            throw new IllegalArgumentException("invalid Id");
        } else {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM dosv.journal_record WHERE id=?");
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("invalid ID");
            }
            return new JournalRecord(results);
        }
    }

    /**
     * Gibt alle Protokolleinträge mit dem spezifizierten Typ und der ID eines Objekts zurück.
     *
     * @param objectType Typ des Objekts.
     * @param objectId ID des Objekts.
     * @return Liste der Protokolleinträge.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public List<JournalRecord> getJournal(ObjectType objectType, int objectId) throws
            SQLException {
        String sql;
        if (objectId < 0) {
            throw new IllegalArgumentException("invalid objectId");
        } else {
            if (objectType == null) {
                sql = "SELECT * FROM dosv.journal_record WHERE object_type IS NULL AND "
                    + "object_id=?";
            } else {
                sql = "SELECT * FROM dosv.journal_record WHERE object_type = '" +
                    objectType + "' AND object_id=?";
            }
            PreparedStatement statement = this.db.prepareStatement(sql);
            statement.setInt(1, objectId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results));
            }
            return journal;
        }
    }

    /**
     * Gibt alle Protokolleinträge mit der Nutzer-ID zurück.
     *
     * @param userId ID des Nutzers.
     * @return Liste der Protokolleinträge.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public List<JournalRecord> getJournal(int userId) throws SQLException {
         if (userId == 0 || userId > 0) {
            PreparedStatement statement = this.db.prepareStatement("SELECT * FROM "
                + "dosv.journal_record WHERE user_id=?");
            statement.setInt(1, userId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results));
            }
            return journal;
        } else {
            throw new IllegalArgumentException("invalid userId");
        }
    }
}
