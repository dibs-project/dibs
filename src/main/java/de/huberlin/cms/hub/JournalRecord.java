/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * JournalRecord Klasse - Eintrag des Logsbuch.
 *
 * @author haphuong
 */
public class JournalRecord {

    /**
     * Typ der Aktion.
     */
    enum ActionType {
        /** Bewerber erstellen. */
        USER_CREATED
    };

    /**
     * Typ des Objekts.
     */
    enum ObjectType {
        /** Bewerber. */
        APPLICANT
    };

    int id;
    ActionType actionType;
    ObjectType objectType;
    int objectId;
    int userId;
    Timestamp time;
    String detail;

    /**
     * Initialisiert den JournalRecord.
     */
    public JournalRecord(int id, ActionType actionType, ObjectType objectType,
            int objectId, int userId, String detail, Timestamp time) {
        this.id = id;
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.userId = userId;
        this.detail = detail;
        this.time = time;
    }

    /**
     * Initialisiert den JournalRecord via Datenbankcursor.
     *
     * @param results Datenbankzeiger, der auf eine Zeile aus dosv.journal_record verweist.
     * @throws SQLException, falls ein Datenbankzugriffsfehler auftritt.
     */
    public JournalRecord(ResultSet results) throws SQLException {
        this(
            results.getInt("id"),
            ActionType.valueOf((results.getString("action_type"))),
            results.getString("object_type") == null ? null :
                ObjectType.valueOf(results.getString("object_type")),
            results.getInt("object_id"),
            results.getInt("user_id"),
            results.getString("detail"),
            results.getTimestamp("time")
        );
    }

    /**
     * ID.
     */
    public int getId() {
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
    public int getObjectId() {
        return this.objectId;
    }

    /**
     * ID des Nutzers.
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Beschreibung des Eintrages.
     */
    public String getDetail() {
        return this.detail;
    }

}
