package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 */
public class Application {

    /**
     * Bewerbungsstatus
     */
    public enum Status {
        RECEIVED("received"), VALID("valid"), REJECTED("rejected"),
        WITHDRAWN("withdrawn"), ADMITTED("admitted"), ACCEPTED("accepted");

        private final String name;

        Status(String name) {
            this.name = name;
        }
    }

    private String id;
    private Status status;
    private String userId;

    Application(String id, Status status, String userId) {
        this.id = id;
        this.status = status;
        this.userId = userId;
    }

    Application(ResultSet results) throws SQLException {
        this(results.getString("id"), Status.valueOf(results.getString("status")),
            results.getString("userId"));
    }

    /**
     * Eindeutige ID
     */
    public String getId() {
        return this.id;
    }

    /**
     * Status der Bewerbung
     */
    public Status getStatus() {
        return status;
    }

    /**
     * ID des Benutzers, zu dem die Bewerbung gehört
     */
    public String getUserId() {
        return this.userId;
    }
}
