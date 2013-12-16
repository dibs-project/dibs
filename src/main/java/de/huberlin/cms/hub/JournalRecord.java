package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class JournalRecord {
    int id;

    /**
     * Typ der Aktion
     *
     */
    enum ActionType {
        /** Bewerber erstellen */
        USER_CREATED;
    };

    /**
     * Typ des Objekts
     *
     */
    enum ObjectType {
        /** Bewerber */
        APPLICANT
    };

    ActionType actionType;
    ObjectType objectType;
    int objectId;
    int userId;
    Timestamp timestamp;
    String detail;

    /**
     * Initialisiert den JournalRecord.
     */
    public JournalRecord(int id, ActionType actionType, ObjectType objectType,
            int objectId, int userId, Timestamp timestamp, String detail) {
        this.id = id;
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.userId = userId;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    /**
     * Initialisiert den JournalRecord via Datenbankcursor.
     *
     * @param results Datenbankzeiger, der auf eine Zeile aus journal_record verweist.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public JournalRecord(ResultSet results) throws SQLException {
        this(
            results.getInt("id"),
            ActionType.valueOf((results.getString("action_type"))),
            ObjectType.valueOf((results.getString("object_type"))),
            results.getInt("object_id"),
            results.getInt("user_id"),
            results.getTimestamp("timestamp"),
            results.getString("detail")
            );
    }

    /**
     * Typ der Aktion
     */
    public ActionType getActionType() {
        return this.actionType;
    }

    /**
     * Typ des Objekts
     */
    public ObjectType getObjectType() {
        return this.objectType;
    }

    /**
     * ID des Objekts
     */
    public int getObjectId() {
        return this.objectId;
    }

    /**
     * ID des Nutzers
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * Beschreibung des Eintrages
     */
    public String getDetail() {
        return this.detail;
    }

}
