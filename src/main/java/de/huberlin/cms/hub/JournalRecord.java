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
    /** Aktionstyp: ein neuer Benutzer angelegt. */
    public static final String ACTION_TYPE_USER_CREATED = "user_created";
    /** Aktionstyp: eine neue Information für einen Benutzer angelegt. */
    public static final String ACTION_TYPE_INFORMATION_CREATED = "information_created";
    /** Aktionstyp: eine Bewerbung für den Studiengang angelegt. */
    public static final String ACTION_TYPE_COURSE_APPLIED = "course_applied";
    /** Aktionstyp: den Bewerbungstatus bearbeitet. */
    public static final String ACTION_TYPE_APPLICATION_STATUS_SET = "application_status_set";
    /** Aktionstyp: ein neuen Studiengang angelegt. */
    public static final String ACTION_TYPE_COURSE_CREATED = "course_created";
    /** Aktionstyp: eine neue Vergaberegel angelegt und mit dem Studiengang verknüpft. */
    public static final String ACTION_TYPE_COURSE_ALLOCATION_RULE_CREATED =
        "course_allocation_rule_created";
    /** Aktionstyp: eine Quote erstellt und mit der Vergaberegel verknüpft. */
    public static final String ACTION_TYPE_ALLOCATION_RULE_QUOTA_CREATED =
        "allocation_rule_quota_created";
    /** Aktionstyp: ein Kriterium erstellt und zur Sortierung von Bewerbern mit einer Rangliste verknüpft. */
    public static final String ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED =
        "quota_ranking_criterion_added";

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
        return this.service.getUser(this.agentId);
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
