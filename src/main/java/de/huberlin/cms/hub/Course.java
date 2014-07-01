/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Studiengang.
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class Course extends HubObject {
    private String name;
    private int capacity;

    Course(String id, String name, int capacity, ApplicationService service) {
        super(id, service);
        this.name = name;
        this.capacity = capacity;
    }

    Course(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Eintrag über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"), service);
    }

    /**
     * Legt eine Bewerbung auf das Studienangebot an.
     *
     * @param userId ID des Bewerbers
     * @param agent ausführender Benutzer
     * @return die angelegte Bewerbung
     */
    public Application apply(String userId, User agent) {
        try {
            String applicationId = Integer.toString(new Random().nextInt());
            service.getDb().setAutoCommit(false);
            String sql = "INSERT INTO application VALUES(?, ?, ?, ?)";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, applicationId);
            statement.setString(2, userId);
            statement.setString(3, this.id);
            statement.setString(4, Application.STATUS_INCOMPLETE);
            statement.executeUpdate();
            service.getJournal().record(ActionType.APPLIED, ObjectType.COURSE, this.id,
                HubObject.getId(agent), applicationId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
            return service.getApplication(applicationId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Name des Studiengangs.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Kapazität des Studiengangs.
     */
    public int getCapacity() {
        return this.capacity;
    }
}
