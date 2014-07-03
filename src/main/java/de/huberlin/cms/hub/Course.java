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
     * Legt ein neues Vergabeschema an.
     *
     * @param agent ausführender Benutzer
     * @return angelegtes Vergabeschema
     */
    protected AllocationRule createAllocationRule(User agent) {
        try {
            service.getDb().setAutoCommit(false);
            String ruleId = "allocation_rule:" + Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                service.getDb().prepareStatement(
                    "INSERT INTO allocation_rule VALUES (?)");
            statement.setString(1, ruleId);
            statement.executeUpdate();
            service.getJournal().record(ActionType.ALLOCATION_RULE_CREATED,
                ObjectType.COURSE, this.id, HubObject.getId(agent), ruleId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
            this.setAllocationRuleId(this.service.getAllocationRule(ruleId), agent);
            return service.getAllocationRule(ruleId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Aktualisiert die ID des Vergabeschemas des Studiengangs.
     * @param rule Vergabeschema
     * @param agent ausführender Benutzer
     * @return aktualisierten Studiengang
     */
    protected Course setAllocationRuleId(AllocationRule rule, User agent) {
        try {
            service.getDb().setAutoCommit(false);
            String ruleId = rule.getId();
            PreparedStatement statement =
                service.getDb().prepareStatement(
                    "UPDATE course SET allocation_rule_id = ? WHERE id = ?");
            statement.setString(1, ruleId);
            statement.setString(2, this.id);
            statement.executeUpdate();
            service.getJournal().record(ActionType.COURSE_UPDATED, ObjectType.COURSE,
                this.id, HubObject.getId(agent), ruleId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
            return service.getCourse(this.id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * ID des Vergabeschemas des Studiengangs.
     */
    public String getAllocationRuleId() {
        try {
            PreparedStatement statement =
                service.getDb().prepareStatement(
                    "SELECT allocation_rule_id FROM course WHERE id = ?");
            statement.setString(1, this.id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException(
                    "illegal id: allocation rule does not exist");
            }
            return results.getString(1);
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
