/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.Date;
import java.util.Map;

/**
 * Einstellungen und globale Daten des Bewerbungssystems.
 *
 * @author Sven Pfaller
 */
public class Settings extends HubObject {
    private String semester;
    private String storageVersion;
    private Date dosvApplicantsUpdateTime;
    private Date dosvApplicationsUpdateTime;

    Settings(Map<String, Object> args) {
        super((String)args.get("id"), (ApplicationService)args.get("service"));
        this.semester = (String)args.get("semester");
        this.storageVersion = (String)args.get("storage_version");
        this.dosvApplicantsUpdateTime = (Date)args.get("dosv_applicants_update_time");
        this.dosvApplicationsUpdateTime = (Date)args.get("dosv_applications_update_time");
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
     * Letzter Update-Zeitpunkt der Bewerber, bzw.&nbsp;Bewerberstammdaten.
     */
    public Date getDosvApplicantsUpdateTime() {
        return this.dosvApplicantsUpdateTime;
    }

    /**
     * Letzer Update-Zeitpunkt der Bewerbungen, bzw.&nbsp;Bewerbungsstatus.
     */
    public Date getDosvApplicationsUpdateTime() {
        return this.dosvApplicationsUpdateTime;
    }
}
