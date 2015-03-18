/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import java.sql.Timestamp;
import java.util.Map;

/**
 * Eintrag im Protokollbuch, der eine Prozessaktion eines Dienstes erfasst.
 *
 * @author Phuong Anh Ha
 */
public class JournalRecord extends DibsObject {

    private String actionType;
    private String objectId;
    private String agentId;
    private Timestamp time;
    private String detail;

    JournalRecord(Map<String, Object> args) {
        super(args);
        this.actionType = (String) args.get("action_type");
        this.objectId = (String) args.get("object_id");
        this.agentId = (String) args.get("agent_id");
        this.time = (Timestamp) args.get("time");
        this.detail = (String) args.get("detail");
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
