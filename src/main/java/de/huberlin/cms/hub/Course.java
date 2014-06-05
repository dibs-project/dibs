/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Studiengang.
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

    public AllocationRule createAllocationRule(String name, User user) {
        return new AllocationRule(name);
    }

    public AllocationRule getAllocationRule(String id) {
        try {
            PreparedStatement statement =
                service.getDb().prepareStatement("SELECT * FROM allocation_rule WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: allocation_rule does not exist");
            }
            return new AllocationRule(results, this);
        } catch (SQLException e) {
            throw new IOError(e);
        }
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
