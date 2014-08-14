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

    private String actionType;
    private String objectId;
    private String agentId;
    private Timestamp time;
    private String detail;

    JournalRecord(String id, String actionType, String objectId, String userId,
            Timestamp time, String detail, ApplicationService service) {
        super(id, service);
        this.actionType = actionType;
        this.objectId = objectId;
        this.agentId = userId;
        this.time = time;
        this.detail = detail;
    }

    JournalRecord(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Eintrag über den Datenbankcursor
        this(
            results.getString("id"),
            results.getString("action_type"),
            results.getString("object_id"),
            results.getString("agent_id"),
            results.getTimestamp("time"),
            results.getString("detail"),
            service
        );
    }

    /**
     * Typ der Aktion.
     */
    public String getActionType() {
        return this.actionType;
    }

    /**
     * ID des Objekts, das die Aktion ausführt.
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * Ausführender Benutzer.
     */
    public User getAgent() {
        return this.agentId != null ? this.service.getUser(this.agentId) : null;
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
