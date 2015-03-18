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

import java.util.Date;
import java.util.Map;
import java.sql.Timestamp;

/**
 * Einstellungen und globale Daten des Bewerbungssystems.
 *
 * @author Sven Pfaller
 * @author Markus Michler
 */
public class Settings extends DibsObject {
    private String semester;
    private String storageVersion;
    private Date dosvSyncTime;
    private Date dosvRemoteApplicationsPullTime;

    Settings(Map<String, Object> args) {
        super(args);
        this.semester = (String) args.get("semester");
        this.storageVersion = (String) args.get("storage_version");
        this.dosvSyncTime = new Date(((Timestamp) args.get("dosv_sync_time")).getTime());
        this.dosvRemoteApplicationsPullTime =
            args.get("dosv_remote_applications_pull_time") == null ? null : new Date(
                ((Timestamp) args.get("dosv_remote_applications_pull_time")).getTime());
    }

    /**
     * Aktuelles Semester im Format <code>JJJJTT</code>, z.B.&nbsp<code>2014WS</code>.
     * <code>J</code> entspricht also dem Jahr und <code>T</code> dem Typ
     * (<code>WS</code> = Wintersemester, <code>SS</code> = Sommersemester).
     */
    public String getSemester() {
        return semester;
    }

    /**
     * Aktuelle Version des Datenspeichers.
     */
    public String getStorageVersion() {
        return storageVersion;
    }

    /**
     * Letzter Update-Zeitpunkt der mit dem DoSV synchronisierten Daten.
     */
    public Date getDosvSyncTime() {
        return new Date(dosvSyncTime.getTime());
    }

    /**
     * Server timestamp of the last DoSV application pull.
     */
    public Date getDosvApplicationsServerTime() {
        return dosvRemoteApplicationsPullTime == null ? null : new Date(
            dosvRemoteApplicationsPullTime.getTime());
    }
}
