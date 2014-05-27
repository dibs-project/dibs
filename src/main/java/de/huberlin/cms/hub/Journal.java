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
 * Respräsentiert das Protokollbuch, welches die gesamten Prozessaktionen eines Dienstes
 * erfasst.
 *
 * @author Phuong Anh Ha
 */
public class Journal {
    private ApplicationService service;
    private Connection db;

    /**
     * Initialisiert das Protokollbuch, damit es den Bewerbungsdienst protokolliert.
     *
     * @param service Bewerbungsdienst
     * @see ApplicationService#getJournal()
     */
    public Journal(ApplicationService service) {
        this.service = service;
        this.db = service.getDb();
    }

    /**
     * Schreibt einen Protokolleintrag in das Protokollbuch.
     *
     * @param actionType Typ der ausgeführten Aktion
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @param userId ID des Nutzers
     * @param detail Beschreibung des Protokolleintrags
     * @return Protokolleintrag
     * @throws NullPointerException wenn kein Aktionstyp <code>actionType</code>
     *     übergeben wurde
     * @throws IllegalArgumentException wenn es sich bei dem Eintrag um kein Objekt 
     *     handelt und wenn <code>objectId</code> oder <code>objectType</code> 
     *     nicht null ist
     */
    public JournalRecord record(ActionType actionType, ObjectType objectType,
            String objectId, String userId, String detail) {
        if (actionType == null) {
            throw new NullPointerException("illegal actionType: null");
        }
        if ((objectType != null && objectId == null) ||
                (objectType == null && objectId != null)) {
            throw new IllegalArgumentException("illegal objectType: empty");
        }

        try {
            Timestamp time = new Timestamp(new Date().getTime());
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
     * @return Protokolleintrag mit der spezifizierten ID
     * @throws IllegalArgumentException wenn kein Protokolleintrag mit der spezifizierten
     *     ID existiert
     */
    public JournalRecord getRecord(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM journal_record WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: record does not exist");
            }
            return new JournalRecord(results);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Protokolleinträge für das spezifierte Objekt zurück.
     *
     * @param objectType Typ des Objekts
     * @param objectId ID des Objekts
     * @return Protokoll für das spezifizierte Objekt
     * @throws NullPointerException wenn ein <code>objectType</code> übergeben wurde
     *     aber die dazugehörige <code>objectId</code> fehlt
     * @throws IllegalArgumentException wenn eine <code>objectId</code> übergeben wurde
     *     aber der dazugehörige <code>objectType</code> fehlt
     */
    public List<JournalRecord> getJournal(ObjectType objectType, String objectId) {
        try {
            List<JournalRecord> journal = new ArrayList<JournalRecord>();
            PreparedStatement statement = null;

            if (objectType != null) {
                if (objectId == null) {
                    throw new NullPointerException("illegal objectId: null");
                }
                String sql = "SELECT * FROM journal_record WHERE object_type = ? AND object_id = ?";
                statement = this.db.prepareStatement(sql);
                statement.setObject(1, objectType);
                statement.setString(2, objectId);
            } else if (objectType == null) {
                if (objectId != null) {
                    throw new IllegalArgumentException("illegal objectId: not null");
                }
                String sql = "SELECT * FROM journal_record WHERE object_type IS NULL AND object_id IS NULL";
                statement = this.db.prepareStatement(sql);
            }

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results));
            }
            return journal;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Protokolleinträge für die spezifizierte Nutzer-ID zurück.
     *
     * @param userId ID des Nutzers
     * @return Protokolleinträge des Nutzers
     */
    public List<JournalRecord> getJournal(String userId) {
        try {
            List<JournalRecord> journal = new ArrayList <JournalRecord>();
            PreparedStatement statement;

            if (userId != null) {
                String sql = "SELECT * FROM journal_record WHERE user_id = ?";
                statement = this.db.prepareStatement(sql);
                statement.setString(1, userId);
            } else {
                String sql = "SELECT * FROM journal_record WHERE user_id IS NULL";
                statement = this.db.prepareStatement(sql);
            }

            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results));
            }
            return journal;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
