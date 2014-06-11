/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;

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
    private Connection db;

    /**
     * Initialisiert den Studiengang.
     */
    public Course(String id, String name, int capacity, ApplicationService service) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.service = service;
        this.db = this.service.getDb();
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
     * Legt ein neues Vergabeschema an.
     * @param name name des Vergabeschemas
     * @param user Benutzer, der das Vergabeschema anlegt
     * @return angelegtes Vergabeschema
     */
    public AllocationRule createAllocationRule(String name, User user) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }

        try {
            this.db.setAutoCommit(false);
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                this.db.prepareStatement("INSERT INTO allocation_rule VALUES(?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
            this.service.getJournal().record(ActionType.USER_CREATED, null, null, null, id);
            this.db.commit();
            this.db.setAutoCommit(true);
            return this.getAllocationRule(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt das Vergabeschema mit der identifizierten ID
     * @param id ID des Vergabeschemas
     * @return
     */
    public AllocationRule getAllocationRule(String id) {
        try {
            PreparedStatement statement =
                service.getDb().prepareStatement("SELECT * FROM allocation_rule WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: allocation_rule does not exist");
            }
            return new AllocationRule(results, service);
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
