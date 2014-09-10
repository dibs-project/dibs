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
    private String storageFormat;
    private Date dosvApplicantsUpdateTime;
    private Date dosvApplicationsUpdateTime;

    Settings(String id, String semester, String storageFormat,
            Date dosvApplicantsUpdateTime, Date dosvApplicationsUpdateTime,
            ApplicationService service) {
        super(id, service);
        this.semester = semester;
        this.storageFormat = storageFormat;
        this.dosvApplicantsUpdateTime = dosvApplicantsUpdateTime;
        this.dosvApplicationsUpdateTime = dosvApplicationsUpdateTime;
    }

    Settings(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Einstellungen über den Datenbankcursor
        this(results.getString("id"), results.getString("semester"),
            results.getString("storage_format"),
            results.getTimestamp("dosv_applicants_update_time"),
            results.getTimestamp("dosv_applications_update_time"), service);
    }

    /**
     * Aktuelles Semester im Format <code>JJJJTT</code>, z.B.&nbsp<code>2014WS</code>.
     * <code>J</code> entspricht also dem Jahr und <code>T</code> dem Typ
     * (<code>WS</code> = Wintersemester, <code>SS</code> = Sommersemester).
     */
    public String getSemester() {
        return semester;
    }

    // TODO: document
    public String getStorageFormat() {
        return storageFormat;
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
