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

import de.huberlin.cms.hub.HubException.ObjectNotFoundException;

/**
 * Respräsentiert das Protokollbuch, welches die gesamten Prozessaktionen des
 * Bewerbungsdienstes erfasst.
 *
 * @author Phuong Anh Ha
 */
public class Journal {
    private ApplicationService service;
    private Connection db;

    Journal(ApplicationService service) {
        this.service = service;
        this.db = service.getDb();
    }

    /**
     * Schreibt einen Protokolleintrag in das Protokollbuch.
     *
     * @param actionType Typ der ausgeführten Aktion
     * @param objectId ID des Objekts, das die Aktion ausführt
     * @param agentId ID des Nutzers, der die Aktion ausführt
     * @param detail Detailbeschreibung
     * @return Protokolleintrag
     */
    public JournalRecord record(String actionType, String objectId, String agentId,
            String detail) {
        try {
            Timestamp time = new Timestamp(new Date().getTime());
            String id = "journal_record:" + Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO journal_record VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, actionType);
            statement.setString(3, objectId);
            statement.setString(4, agentId);
            statement.setTimestamp(5, time);
            statement.setString(6, detail);
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
     */
    public JournalRecord getRecord(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM journal_record WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new ObjectNotFoundException(id);
            }
            return new JournalRecord(results, this.service);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Protokolleinträge für die spezifierte Objekt-ID zurück.
     *
     * @param objectId ID des Objekts
     * @return Protokolleinträge des Objektes
     */
    public List<JournalRecord> getRecordsByObjectId(String objectId) {
        try {
            List<JournalRecord> journal = new ArrayList<JournalRecord>();
            PreparedStatement statement = null;
            if (objectId == null) {
                statement = this.db.prepareStatement(
                    "SELECT * FROM journal_record WHERE object_id IS NULL");
            } else {
                statement = this.db.prepareStatement(
                    "SELECT * FROM journal_record WHERE object_id = ?");
                statement.setString(1, objectId);
            }
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results, this.service));
            }
            return journal;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Protokolleinträge für die spezifizierte Nutzer-ID zurück.
     *
     * @param agentId ID des ausführenden Nutzers
     * @return Protokolleinträge des Nutzers
     */
    public List<JournalRecord> getRecordsByAgentId(String agentId) {
        try {
            List<JournalRecord> journal = new ArrayList<JournalRecord>();
            PreparedStatement statement = null;
            if (agentId == null) {
                statement = this.db.prepareStatement(
                    "SELECT * FROM journal_record WHERE agent_id IS NULL");
            } else {
                statement = this.db.prepareStatement(
                    "SELECT * FROM journal_record WHERE agent_id = ?");
                statement.setString(1, agentId);
            }
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                journal.add(new JournalRecord(results, this.service));
            }
            return journal;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
