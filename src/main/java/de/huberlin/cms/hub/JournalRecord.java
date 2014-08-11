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
    public static final String TYPE_USER_CREATED = "user_created";
    public static final String TYPE_INFORMATION_CREATED = "information_created";
    public static final String TYPE_COURSE_APPLIED = "course_applied";
    public static final String TYPE_APPLICATION_STATUS_SET = "application_status_set";
    public static final String TYPE_COURSE_CREATED = "course_created";
    public static final String TYPE_COURSE_ALLOCATION_RULE_CREATED = "course_allocation_rule_created";
    public static final String TYPE_ALLOCATION_RULE_QUOTA_CREATED = "allocation_rule_quota_created";
    public static final String TYPE_QUOTA_RANKING_CRITERION_ADDED = "quota_ranking_criterion_added";

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
     * Typ der Aktion.</br>
     * Konstanten:
     * <ul>
     * <li><code>user_created</code>: neuer Benutzer erstellen
     * <li><code>information_created</code>: Information erstellen
     * <li><code>course_applied</code>: Studiengang bewerben
     * <li><code>application_status_set</code>: Bewerbungstatus bearbeiten
     * <li><code>course_created</code>: Studiengang anlegen
     * <li><code>course_allocation_rule_created</code>: Vergabeschema anlegen
     * <li><code>allocation_rule_quota_created</code>: Ranglsiten anlegen
     * <li><code>quota_ranking_criterion_added</code>: Kriterium anlegen
     * </ul>
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
     * ID des Nutzers, der die Aktion ausführt.
     */
    public String getAgentId() {
        return this.agentId;
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
