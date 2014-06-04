/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Studiengang des Bewerbungsdienst
 * @author Phuong Anh Ha 
 *
 */
public class Course {
    private String id;
    private String name;
    private int capacity;
    private ApplicationService service;

    /**
     * Initialisiert den Studiengang.
     */
    Course(String id, String name, int capacity, ApplicationService service) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.service = service;
    }

    /**
     * Initialisiert den Course via Datenbankcursor.
     *
     * @param results Datenbankcursor, der auf eine Zeile aus <code>course</code> verweist
     * @param service Bewerbungdienst
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    Course(ResultSet results, ApplicationService service) throws SQLException {
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"), service);
    }

    /**
     * Eindeutige ID.
     */
    public String getId() {
        return this.id;
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
    public int capacity() {
        return this.capacity;
    }
}
