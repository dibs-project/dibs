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
 * @author Markus Michler
 */
public class Course extends HubObject {
    private String name;
    private int capacity;
    private AllocationRule allocationRule;

    Course(String id, String name, int capacity, String allocationRuleId,
            ApplicationService service) {
        super(id, service);
        this.name = name;
        this.capacity = capacity;
        if (allocationRuleId == null) {
            this.allocationRule = null;
        } else {
            this.allocationRule = service.getAllocationRule(allocationRuleId);
        }
    }

    Course(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Studiengang über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"), results.getString("allocation_rule_id"), service);
    }

    /**
     * Legt eine neue Vergaberegel an und verknüpft diese mit dem Studiengang.
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
            sql = "UPDATE course SET allocation_rule_id = ? WHERE id = ?";
            statement = db.prepareStatement(sql);
            statement.setString(1, ruleId);
            statement.setString(2, this.id);
            statement.executeUpdate();
            db.commit();
            db.setAutoCommit(true);
            this.allocationRule = service.getAllocationRule(ruleId);
            service.getJournal().record(ActionType.ALLOCATION_RULE_CREATED,
                ObjectType.COURSE, this.id, HubObject.getId(agent), ruleId);
            return this.allocationRule;
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
     * Vergaberegel des Studiengangs.
     */
    public AllocationRule getAllocationRule() {
        return this.allocationRule;
    }
}
