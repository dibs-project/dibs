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
import de.huberlin.cms.hub.JournalRecord.ObjectType;

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
        // initialisiert den Studiengang über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"), service);
    }

    /**
     * Legt eine neue Vergaberegel an und verknüpft diese mit dem Studienangebot.
     *
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Vergaberegel
     */
    public AllocationRule createAllocationRule(User agent) {
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String ruleId = "allocation_rule:" + Integer.toString(new Random().nextInt());
            String sql = "INSERT INTO allocation_rule VALUES (?)";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, ruleId);
            statement.executeUpdate();
            service.getJournal().record(ActionType.ALLOCATION_RULE_CREATED,
                ObjectType.COURSE, this.id, HubObject.getId(agent), ruleId);
            sql = "UPDATE course SET allocation_rule_id = ? WHERE id = ?";
            statement = db.prepareStatement(sql);
            statement.setString(1, ruleId);
            statement.setString(2, this.id);
            statement.executeUpdate();
            db.commit();
            db.setAutoCommit(true);
            return service.getAllocationRule(ruleId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Vergaberegel zurück.
     */
    public AllocationRule getAllocationRule() {
        try {
            String sql = "SELECT allocation_rule_id FROM course WHERE id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, this.id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                return null;
            }
            return service.getAllocationRule(results.getString(1));
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
