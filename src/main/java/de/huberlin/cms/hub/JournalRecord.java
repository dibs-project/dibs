/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Eintrag im Protokollbuch, der eine Prozessaktion eines Dienstes erfasst.
 *
 * @author Phuong Anh Ha
 */
public class JournalRecord {
    /**
     * Typ der Aktion des Dienstes.
     */
    public enum ActionType {
        USER_CREATED
    };

    /**
     * Typ des Objekts des Dienstes.
     */
    public enum ObjectType {
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
     * @param results Datenbankcursor, der auf eine Zeile aus <code>journal_record</code>
     *     verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    JournalRecord(ResultSet results) throws SQLException {
        this(
            results.getString("id"),
            ActionType.valueOf(results.getString("action_type")),
            Util.valueOfEnum(ObjectType.class, results.getString("object_type")),
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
     * Typ des Objekts, das die Aktion ausführt.
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * ID des Objekts, das die Aktion ausführt.
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * ID des Nutzers, der die Aktion ausführt.
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
     * Detailbeschreibung.
     */
    public String getDetail() {
        return this.detail;
    }
}
