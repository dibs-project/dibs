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
public class JournalRecord extends HubObject {
    /**
     * Typ der Aktion des Dienstes.
     */
    public enum ActionType {
        USER_CREATED,
        COURSE_CREATED,
        ALLOCATION_RULE_CREATED
    };

    /**
     * Typ des Objekts des Dienstes.
     */
    public enum ObjectType {
        USER,
        COURSE,
        ALLOCATION_RULE
    };

    private ActionType actionType;
    private ObjectType objectType;
    private String objectId;
    private String userId;
    private Timestamp time;
    private String detail;

    JournalRecord(String id, ActionType actionType, ObjectType objectType,
            String objectId, String userId, Timestamp time, String detail,
            ApplicationService service) {
        super(id, service);
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.userId = userId;
        this.time = time;
        this.detail = detail;
    }

    JournalRecord(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Eintrag über den Datenbankcursor
        this(
            results.getString("id"),
            ActionType.valueOf(results.getString("action_type")),
            Util.valueOfEnum(ObjectType.class, results.getString("object_type")),
            results.getString("object_id"),
            results.getString("user_id"),
            results.getTimestamp("time"),
            results.getString("detail"),
            service
        );
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
