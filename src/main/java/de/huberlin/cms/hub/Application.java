/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.HashMap;

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

    private String userId;
    private String courseId;
    private String status;

    Application(HashMap<String, Object> args) {
        super((String)args.get("id"), (ApplicationService)args.get("service"));
        this.userId = (String)args.get("user_id");
        this.courseId = (String)args.get("course_id");
        this.status = (String)args.get("status");
    }

    /**
     * ID des Benutzers, zu dem die Bewerbung gehört.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * ID des Studienangebots, auf das der Benutzer sich beworben hat.
     */
    public String getCourseId() {
        return this.courseId;
    }

    /**
     * Status der Bewerbung.</br>
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
}
