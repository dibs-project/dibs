/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
            service.getQueryRunner().insert(service.getDb(),
                "INSERT INTO journal_record VALUES (?, ?, ?, ?, ?, ?)",
                service.getMapHandler(), id, actionType, objectId, agentId, time, detail);
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
            Map<String, Object> args = service.getQueryRunner().query(this.db,
                "SELECT * FROM journal_record WHERE id = ?", service.getMapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", service);
            return new JournalRecord(args);
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
            String sql = null;
            List<JournalRecord> journal = new ArrayList<JournalRecord>();
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            if (objectId == null) {
                sql = "SELECT * FROM journal_record WHERE object_id IS NULL";
                queryResults = service.getQueryRunner().query(service.getDb(), sql,
                    service.getMapListHandler());
            } else {
                sql = "SELECT * FROM journal_record WHERE object_id = ?";
                queryResults = service.getQueryRunner().query(service.getDb(), sql,
                    service.getMapListHandler(), objectId);
            }
            for (Map<String, Object> args : queryResults) {
                args.put("service", service);
                journal.add(new JournalRecord(args));
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
            String sql = null;
            List<JournalRecord> journal = new ArrayList<JournalRecord>();
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            if (agentId == null) {
                sql = "SELECT * FROM journal_record WHERE agent_id IS NULL";
                queryResults = service.getQueryRunner().query(service.getDb(), sql,
                    service.getMapListHandler());
            } else {
                sql = "SELECT * FROM journal_record WHERE agent_id = ?";
                queryResults = service.getQueryRunner().query(service.getDb(), sql,
                    service.getMapListHandler(), agentId);
            }
            for (Map<String, Object> args : queryResults) {
                args.put("service", service);
                journal.add(new JournalRecord(args));
            }
            return journal;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
