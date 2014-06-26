/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

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
     * @param applicant
     * @param agent
     * @return
     */
    public Application apply(String userId, User agent) {
        HashMap<String, Object> args = new HashMap<String, Object>();
        // TODO Daten aus DB
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
