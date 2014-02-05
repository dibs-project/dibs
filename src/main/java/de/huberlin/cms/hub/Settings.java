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
 * @author pfallers
 */
public class Settings {
    private String semester;
    private Date dosvApplicantsUpdateTime;
    private Date dosvApplicationsUpdateTime;

    /**
     * Initialisiert die Settings.
     */
    public Settings(String semester, Date dosvApplicantsUpdateTime,
            Date dosvApplicationsUpdateTime) {
        this.semester = semester;
        this.dosvApplicantsUpdateTime = dosvApplicantsUpdateTime;
        this.dosvApplicationsUpdateTime = dosvApplicationsUpdateTime;
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

    /**
     * Initialisiert die Settings via Datenbankcursor.
     *
     * @param results Datenbankcursor, der auf eine Zeile aus dosv.settings verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    Settings(ResultSet results) throws SQLException {
        this(results.getString("semester"),
            results.getDate("dosv_applicants_update_time"),
            results.getDate("dosv_applications_update_time"));
    }
}
