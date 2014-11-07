/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Einstellungen und globale Daten des Bewerbungssystems.
 *
 * @author Sven Pfaller
 */
public class Settings extends HubObject {
    private String semester;
    private String storageVersion;
    private Date dosvSyncTime;

    Settings(String id, String semester, String storageVersion,
            Date dosvUpdateTime,
            ApplicationService service) {
        super(id, service);
        this.semester = semester;
        this.storageVersion = storageVersion;
        this.dosvSyncTime = dosvUpdateTime;
    }

    Settings(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Einstellungen über den Datenbankcursor
        this(results.getString("id"), results.getString("semester"),
            results.getString("storage_version"),
            results.getTimestamp("dosv_sync_time"), service);
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
        return this.dosvSyncTime;
    }
}
