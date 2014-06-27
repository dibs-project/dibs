/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;

/**
 * Studiengang.
 *
 * @author Phuong Anh Ha
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
     * TODO
     *
     * @param userId
     * @param agent
     * @return
     */
    public Application apply(String userId, User agent) {
        // TODO Objekterstellung in getter auslagern
        // TODO user validieren?
        HashMap<String, Object> args = new HashMap<String, Object>();
        String applicationId = Integer.toString(new Random().nextInt());
        args.put("id", applicationId);
        args.put("service", service);
        args.put("user_id", userId);
        args.put("course_id", id);
        args.put("status", Application.STATUS_INCOMPLETE);
        try {
            service.getDb().setAutoCommit(false);
            String sql = "INSERT INTO application VALUES(?, ?, ?, ?)";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, applicationId);
            statement.setString(2, userId);
            statement.setString(3, this.id);
            statement.setString(4, Application.STATUS_INCOMPLETE);
            statement.executeUpdate();
            // TODO Journal COURSE_APPLIED
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        // TODO getApplication(s)
        return new Application(args);
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
