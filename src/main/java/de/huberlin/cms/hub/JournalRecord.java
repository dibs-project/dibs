package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class JournalRecord {
    int record_id;
    enum ActionType {
        /* Stammdaten aktualisieren */
        USER_PROFILE_UPDATE,
        /* Bewerbungsstatus ändern */
        APLICATION_STATUS_EDITED,
        /* Bewerber erstellen */
        USER_CREATED;
    };

    enum ObjectType {
        /* Bewerber */
        APPLICANT,
        /* Bewerbung */
        APPLICATION,
        /* Studienangebot*/
        COURSE;
    };

    ActionType actionType;
    ObjectType objectType;
    int objectID;
    int userID;
    Timestamp timestamp;
    String detail;

    /**
     * Initialisiert den JournalRecord
     * @param actionTypeID
     * @param detail
     * @param objectID
     * @param objectTypeID
     * @param userID
     */
    public JournalRecord(int record_id, ActionType actionType, ObjectType objectType, 
            int objectID, int userID, Timestamp timestamp, String detail) {
        this.record_id = record_id;
        this.actionType = actionType;
        this.objectType = objectType;
        this.objectID = objectID;
        this.userID = userID;
        this.detail = detail;
        this.timestamp = timestamp;
    }

    /**
     * Initialisiert den JournalRecord via Datenbankcursor.
     *
     * @param results Datenbankzeiger, der auf eine Zeile aus onlbew_reg verweist.
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public JournalRecord(ResultSet results) throws SQLException {
        this(
            results.getInt("record_id"),
            ActionType.valueOf((results.getString("action_type"))),
            ObjectType.valueOf((results.getString("object_type"))),
            results.getInt("object_id"),
            results.getInt("user_id"),
            results.getTimestamp("timestamp"),
            results.getString("detail")
            );
    }

    public int getObjectID() {
        return this.objectID;
    }

    public ActionType getActionType() {
        return this.actionType;
    }

    public ObjectType getObjectType() {
        return this.objectType;
    }

    public int getUserID() {
        return this.userID;
    }

    public String getDetail() {
        return this.detail;
    }

}
