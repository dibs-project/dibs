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

/**
 * Studiengang.
 *
 * @author Phuong Anh Ha
 */
public class Course extends HubObject {
    private String name;
    private int capacity;
    private AllocationRule rule;

    Course(String id, String name, int capacity, AllocationRule rule,
            ApplicationService service) {
        super(id, service);
        this.name = name;
        this.capacity = capacity;
        this.rule = rule;
    }

    Course(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Studiengang über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"),
            service.getAllocationRule(results.getString("allocation_rule_id")),
            service);
    }

    /**
     * Legt ein neues Vergabeschema an.
     *
     * @param agent ausführender Benutzer
     * @return angelegtes Vergabeschema
     */
    protected static AllocationRule createAllocationRule(User agent) {
        try {
            HubObject.service.getDb().setAutoCommit(false);
            String id = "allocation_rule:" + Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                HubObject.service.getDb().prepareStatement("INSERT INTO allocation_rule VALUES(?)");
            statement.setString(1, id);
            statement.executeUpdate();
            HubObject.service.getJournal().record(ActionType.ALLOCATION_RULE_CREATED, null,
                null, HubObject.getId(agent), id);
            HubObject.service.getDb().commit();
            HubObject.service.getDb().setAutoCommit(true);
            return HubObject.service.getAllocationRule(id);
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

    /**
     * Vergabeschema des Studiengangs.
     */
    public AllocationRule getAllocationRule() {
        return this.rule;
    }
}
