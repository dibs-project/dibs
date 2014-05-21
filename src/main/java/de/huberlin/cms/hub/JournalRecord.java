/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Eintrag im Protokollbuch, den eine Prozessaktion eines Dienstes aufschreibt.
 *
 * @author Phuong Anh Ha
 */
public class JournalRecord {
    /**
     * Typ der Aktion des Bewerbungsdienstes.
     */
    public enum ActionType {
        /** Nutzer erstellen. */
        USER_CREATED
    };

    /**
     * Typ des Objekts des Bewerbungsdienstes.
     */
    public enum ObjectType {
        /** Benutzer. */
        USER
    };

    private String id;
    private ActionType actionType;
    private ObjectType objectType;
    private String objectId;
    private String userId;
    private Timestamp time;
    private String detail;

    /**
     * Initialisiert den Protokolleintrag.
     */
    public JournalRecord(String id, ActionType actionType, ObjectType objectType,
            String objectId, String userId, Timestamp time, String detail) {
        this.id = id;
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.userId = userId;
        this.time = time;
        this.detail = detail;
    }

    /**
     * Initialisiert den Protokolleintrag über den Datenbankcursor.
     *
     * @param results Datenbankzeiger, der auf eine Zeile aus <code>journal_record</code>
     * verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    JournalRecord(ResultSet results) throws SQLException {
        this(
            results.getString("id"),
            ActionType.valueOf(results.getString("action_type")),
            //TODO: Utility
            results.getString("object_type") == null ? null :
                ObjectType.valueOf(results.getString("object_type")),
            results.getString("object_id"),
            results.getString("user_id"),
            results.getTimestamp("time"),
            results.getString("detail")
        );
    }

    /**
     * ID des Protokolleintrags.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Typ der Aktion.
     */
    public ActionType getActionType() {
        return this.actionType;
    }

    /**
     * Typ des Objekts.
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * ID des Objekts.
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * ID des Nutzers.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Zeitstempel.
     */
    public Timestamp getTime() {
        return this.time;
    }

    /**
     * Beschreibung des Protokolleintrags.
     */
    public String getDetail() {
        return this.detail;
    }
}
