/*
 * hub
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Einstellungen und globale Daten des Bewerbungssystems.
 */
public class Settings {
    String semester;
    Date dosvUsersUpdated;
    Date dosvApplicationsUpdated;

    /**
     * Initialisiert die Settings.
     */
    public Settings(String semester, Date dosvUsersUpdated,
            Date dosvApplicationsUpdated) {
        this.semester = semester;
        this.dosvUsersUpdated = dosvUsersUpdated;
        this.dosvApplicationsUpdated = dosvApplicationsUpdated;
    }

    /**
     * Initialisiert die Settings via Datenbankcursor.
     *
     * @param results Datenbankcursor, der auf eine Zeile aus dosv.settings verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    Settings(ResultSet results) throws SQLException {
        this(results.getString("semester"), results.getDate("dosv_users_updated"),
            results.getDate("dosv_applications_updated"));
    }

    /**
     * Aktuelles Semester im Format <code>JJJJTT</code>, z.B. <code>2014WS</code>.
     * <code>J</code> entspricht also dem Jahr und <code>T</code> dem Typ
     * (<code>WS</code> = Wintersemester, <code>SS</code> = Sommersemester).
     */
    public String getSemester() {
        return semester;
    }
}
