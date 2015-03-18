/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
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
