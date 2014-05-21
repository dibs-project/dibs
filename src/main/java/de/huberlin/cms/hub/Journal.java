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
import java.util.Date;
import java.util.List;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Respräsentiert das Protokollbuch, welches die gesamte Prozessaktionen eines Dienstes
 * aufschreibt.
 *
 * @author Phuong Anh Ha
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
        this.db = service.getDb();
    }

    /**
     * Schreibt einen Protokolleintrag ins Protokollbuch.
     *
     * @param actionType Typ der ausgeführten Aktion
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @param userId ID des Nutzers
     * @param detail Beschreibung des Protokolleintrags
     * @return den Protokolleintrag
     * @throws SQLException falls ein DatenbankABFRAGEfehler auftritt  ????
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            String objectId, String userId, String detail) {
        //TODO: test SQLException wenn actionType==null
        if (actionType == null) {
            throw new NullPointerException("illegal actionType: null");
        }
        if (objectId == null&&objectType!=null) {
            throw new IllegalArgumentException("illegal userID: empty");
        }

        Timestamp time = new Timestamp(new Date().getTime());

        try {
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO journal_record VALUES (?, ?, ?, ?, ?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, actionType.toString());
            statement.setString(3, objectType == null ? null : objectType.toString());
            statement.setString(4, objectId);
            statement.setString(5, userId);
            statement.setTimestamp(6, time);
            statement.setString(7, detail);
            System.out.println(statement);
            statement.executeUpdate();
            return this.getRecord(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt den Protokolleintrag mit der spezifizierten ID zurück.
     *
     * @param id ID des Protokolleintrags
     * @return den Protokolleintrag mit der spezifizierten ID
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public JournalRecord getRecord(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM journal_record WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal Id: record does not exist");
            }
            return new JournalRecord(results);
        }
        catch (SQLException e)                                                                                                                                                                                                                                                                                            {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Protokolleinträge mit dem spezifizierten Typ und der ID des Objektes
     * zurück.
     *
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @return alle Protokolleinträge mit dem spezifizierten Typ und der ID des Objekts
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public List<JournalRecord> getJournal(ObjectType objectType, String objectId) throws
            SQLException {
        List<JournalRecord> journal = new ArrayList <JournalRecord>();
        String sql;
        //TODO: beide?
        if (objectType == null||objectId.isEmpty()) {
            sql = "SELECT * FROM journal_record WHERE object_type IS NULL AND "
                + "object_id IS NULL";
        } else {
            sql = "SELECT * FROM journal_record WHERE object_type = '" + objectType
                + "' AND object_id=?";
        }

        PreparedStatement statement = this.db.prepareStatement(sql);
        statement.setString(1, objectId);
        ResultSet results = statement.executeQuery();
        while (results.next()) {
            journal.add(new JournalRecord(results));
        }
        return journal;
    }

    /**
     * Gibt alle Protokolleinträge mit der Nutzer-ID zurück.
     *
     * @param userId ID des Nutzers
     * @return alle Protokolleinträge, die von dem Nutzer bearbeitet wurden
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public List<JournalRecord> getJournal(String userId) {
        try {
            List<JournalRecord> journal = new ArrayList <JournalRecord>();
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM journal_record WHERE user_id = ?");
            statement.setString(1, userId);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results));
            }
            return journal;
        }
        catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
