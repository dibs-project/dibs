/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 */
public class Application extends HubObject {

    // Konstanten für den Bewerbungsstatus
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_VALID = "valid";
    public static final String STATUS_WITHDRAWN = "withdrawn";
    public static final String STATUS_ADMITTED = "admitted";
    public static final String STATUS_CONFIRMED = "confirmed";

    private final String userId;
    private final String courseId;
    private String status;

    Application(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.userId = (String) args.get("user_id");
        this.courseId = (String) args.get("course_id");
        this.status = (String) args.get("status");
    }

    void setStatus(String status, User agent) {
        this.status = status;
        try {
            service.getDb().setAutoCommit(false);
            String sql = "UPDATE application SET status = ? WHERE id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, status);
            statement.setString(2, this.id);
            statement.executeUpdate();
            service.getJournal().record(ActionType.APPLICATION_STATUS_SET,
                ObjectType.APPLICATION, this.id, HubObject.getId(agent), status);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
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
