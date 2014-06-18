/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;

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
     * Legt ein neues Vergabeschema an.
     *
     * @param name Name des Vergabeschemas
     * @param user Benutzer, der das Vergabeschema anlegt
     * @return angelegtes Vergabeschema
     * @throws IllegalArgumentException wenn <code>name</code> leer ist
     */
    public AllocationRule createAllocationRule(String name, User user) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }

        try {
            this.getService().getDb().setAutoCommit(false);
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                this.service.getDb().prepareStatement("INSERT INTO allocation_rule "
                    + "VALUES(?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.executeUpdate();
            this.service.getJournal().record(ActionType.ALLOCATION_RULE_CREATED, null,
                null, user.getId(), name);
            this.getService().getDb().commit();
            this.getService().getDb().setAutoCommit(true);
            return this.getAllocationRule(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt das Vergabeschema mit der identifizierten ID zurück.
     *
     * @param id ID des Vergabeschemas
     * @return Vergabeschema mit der identifizierten ID
     * @throws IllegalArgumentException wenn kein Vergabeschema mit der identifizierten ID
     *     existiert
     */
    public AllocationRule getAllocationRule(String id) {
        try {
            PreparedStatement statement =
                this.service.getDb().prepareStatement("SELECT * FROM allocation_rule "
                    + "WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: allocation_rule does not "
                    + "exist");
            }
            return new AllocationRule(results, service);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt eine Liste aller Vergabeschemen zurück.
     *
     * @return Liste aller Vergabeschemen
     */
    public List<AllocationRule> getAllocationRules() {
        try {
            ArrayList<AllocationRule> allocations = new ArrayList<AllocationRule>();
            PreparedStatement statement =
                this.service.getDb().prepareStatement("SELECT * FROM allocation_rule");
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                allocations.add(new AllocationRule(results, this.service));
            }
            return allocations;
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
