/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
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
 * Respräsentiert das Protokollbuch, welches die gesamte Prozessaktionen eines Dienstes
 * aufschreibt.
 *
 * @author haphuong
 */
public class Journal {
    private ApplicationService service;
    private Connection db;

    /**
     * Initialisiert den Journal, dass die Prozessaktionen des Bewerbungsdienstes
     * protokolliert und in der Datenbank dieses Dienstes schreibt.
     *
     * @param service Bewerbungsdienst
     * @see ApplicationService#getJournal()
     */
    public Journal(ApplicationService service) {
        this.service = service;
        this.db = service.db;
    }

    /**
     * Schreibt einen Protokolleintrag ins Protokollbuch.
     *
     * @param actionType Typ der ausgeführten Aktion, das nicht leer sein darf.
     * @param objectType Typ des Objekts.
     * @param objectId ID des Objekts, die nicht negativ sein darf.
     * @param userId ID des Nutzers, die nicht negativ sein darf.
     * @param detail Beschreibung des Protokolleintrags.
     * @return den Protokolleintrag.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            int objectId, int userId, String detail) {

        if (actionType == null || objectId < 0 || userId < 0)
            throw new IllegalArgumentException("invalid Action Type or object ID or user ID");

        java.util.Date date = new java.util.Date();
        Timestamp time = new Timestamp(date.getTime());

        try {
            String sql = "INSERT INTO dosv.journal_record "
                + "(action_type, object_type, object_id, user_id, detail, time) "
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

    /**
     * Gibt den Protokolleintrag mit der spezifizierten ID zurück.
     *
     * @param id ID des Protokolleintrags.
     * @return den Protokolleintrag mit der spezifizierten ID.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public JournalRecord getRecord(int id) throws SQLException {
        if (id < 0)
            throw new IllegalArgumentException("invalid record Id");

        PreparedStatement statement =
            this.db.prepareStatement("SELECT * FROM dosv.journal_record WHERE id=?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        if (!results.next()) {
            throw new IllegalArgumentException("invalid record ID");
        }
        return new JournalRecord(results);
    }

    /**
     * Gibt alle Protokolleinträge mit dem spezifizierten Typ und der ID des Objektes
     * zurück.
     *
     * @param objectType Typ des Objekts.
     * @param objectId ID des Objekts.
     * @return alle Protokolleinträge mit dem spezifizierten Typ und der ID des Objekts.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public List<JournalRecord> getJournal(ObjectType objectType, int objectId) throws
            SQLException {
        List<JournalRecord> journal = new ArrayList <JournalRecord>();
        String sql;

        if (objectId < 0)
            throw new IllegalArgumentException("invalid objectId");

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

    /**
     * Gibt alle Protokolleinträge mit der Nutzer-ID zurück.
     *
     * @param userId ID des Nutzers.
     * @return alle Protokolleinträge, die von der Nutzer-ID bearbeitet wurden.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt.
     */
    public List<JournalRecord> getJournal(int userId) throws SQLException {
        List<JournalRecord> journal = new ArrayList <JournalRecord>();

        if (userId < 0)
            throw new IllegalArgumentException("invalid user Id");

        PreparedStatement statement = this.db.prepareStatement("SELECT * FROM "
            + "dosv.journal_record WHERE user_id=?");
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }
}
