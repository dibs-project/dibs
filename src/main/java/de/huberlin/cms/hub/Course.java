/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Studiengang
 *
 * <p>
 * Studiengang ist ein Datenbankobjekt und eine dazugehörige Tabelle hätte folgende
 * Struktur:
 * <pre>
 * CREATE TABLE course (
 *     id VARCHAR(256) PRIMARY KEY,
 *     name VARCHAR(256) NOT NULL,
 *     capacity INT NOT NULL
 * );
 * </pre>
 *
 * @author Phuong Anh Ha
 */
public class Course {
    private String id;
    private String name;
    private int capacity;
    private ApplicationService service;

    /**
     * Initialisiert den Studiengang.
     */
    public Course(String id, String name, int capacity, ApplicationService service) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.service = service;
    }

    /**
     * Initialisiert den Studiengang via Datenbankcursor.
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
    public int getCapacity() {
        return this.capacity;
    }
}
