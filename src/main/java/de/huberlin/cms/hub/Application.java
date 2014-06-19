/**
 * HUB Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 */
public class Application extends HubObject {

    // Konstanten für den Bewerbungsstatus
    public static final String INCOMPLETE = "incomplete";
    public static final String COMPLETE = "complete";
    public static final String VALID = "valid";
    public static final String WITHDRAWN = "withdrawn";
    public static final String ADMITTED = "admitted";
    public static final String CONFIRMED = "confirmed";

    private String status;
    private String userId;

    Application(String id, ApplicationService service, String status, String userId) {
        super(id, service);
        this.status = status;
        this.userId = userId;
    }

    Application(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Benutzer über den Datenbankcursor
        this(results.getString("id"), service, results.getString("status"), results
            .getString("userId"));
    }

    /**
     * Status der Bewerbung</br>
     * Konstanten:
     * <ul>
     * <li><code>incomplete</code>: angelegt, nicht vollständig
     * <li><code>complete</code>: vom Benutzer finalisiert, vollständig
     * <li><code>valid</code>: gültig, nimmt am Zulassungsverfahren teil
     * <li><code>withdrawn</code>: zurückgezogen
     * <li><code>admitted</code>: Zulassungsangebot ausgesprochen
     * <li><code>confirmed</code>: Zulassungsangebot angenommen, zugelassen
     * </ul>
     */
    public String getStatus() {
        return status;
    }

    /**
     * ID des Benutzers, zu dem die Bewerbung gehört
     */
    public String getUserId() {
        return this.userId;
    }
}
