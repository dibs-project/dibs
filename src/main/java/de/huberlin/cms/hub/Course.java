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
import java.sql.Types;
import java.util.List;
import java.util.Random;

/**
 * Studiengang.
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class Course extends HubObject {
    private String name;
    private int capacity;
    private String allocationRuleId;

    Course(String id, String name, int capacity, String allocationRuleId,
        ApplicationService service) {
        super(id, service);
        this.name = name;
        this.capacity = capacity;
        this.allocationRuleId = allocationRuleId;
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
            this.allocationRuleId = ruleId;
            service.getJournal().record(JournalRecord.TYPE_COURSE_ALLOCATION_RULE_CREATED,
                this.id, HubObject.getId(agent), ruleId);
            db.commit();
            db.setAutoCommit(true);
            return service.getAllocationRule(allocationRuleId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Legt eine Bewerbung auf den Studiengang an.
     *
     * @param userId ID des Bewerbers
     * @param agent ausführender Benutzer
     * @return angelegte Bewerbung
     */
    public Application apply(String userId, User agent) {
        // TODO: sicherstellen, dass Metadaten komplett sind (= Vergaberegel und Quote)

        try {
            service.getDb().setAutoCommit(false);
            String applicationId =
                String.format("application:%s", new Random().nextInt());
            String sql = "INSERT INTO application VALUES (?, ?, ?, ?)";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, applicationId);
            statement.setString(2, userId);
            statement.setString(3, this.id);
            statement.setString(4, Application.STATUS_INCOMPLETE);
            statement.executeUpdate();

            // Bewertung für jedes Kriterium der Verteilungsregel erstellen
            // NOTE: Query kann noch optimiert werden
            List<Criterion> criteria =
                this.getAllocationRule().getQuota().getRankingCriteria();
            for (Criterion criterion : criteria) {
                String id = String.format("evaluation:%s", new Random().nextInt());
                statement = this.service.getDb().prepareStatement(
                    "INSERT INTO evaluation VALUES (?, ?, ?, ?, ?, ?)");
                statement.setString(1, id);
                statement.setString(2, applicationId);
                statement.setString(3, criterion.getId());
                statement.setNull(4, Types.VARCHAR);
                statement.setNull(5, Types.DOUBLE);
                statement.setString(6, Evaluation.STATUS_INFORMATION_MISSING);
                statement.executeUpdate();
            }

            service.getJournal().record(JournalRecord.TYPE_COURSE_APPLIED, this.id,
                HubObject.getId(agent), applicationId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
            return service.getApplication(applicationId);

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
        return allocationRuleId != null ? service.getAllocationRule(allocationRuleId) : null;
    }
}
