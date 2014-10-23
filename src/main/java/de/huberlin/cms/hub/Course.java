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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import de.huberlin.cms.hub.HubException.HubObjectIllegalStateException;

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
    private boolean published;

    Course(String id, String name, int capacity, String allocationRuleId, boolean published,
        ApplicationService service) {
        super(id, service);
        this.name = name;
        this.capacity = capacity;
        this.allocationRuleId = allocationRuleId;
        this.published = published;
    }

    Course(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Studiengang über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("capacity"), results.getString("allocation_rule_id"),
            results.getBoolean("published"), service);
    }

    /**
     * Legt eine neue Vergaberegel an und verknüpft diese mit dem Studiengang.
     *
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Vergaberegel
     */
    public AllocationRule createAllocationRule(User agent) {
        if (service.getCourse(id).isPublished()) {
            throw new HubObjectIllegalStateException(getId());
        }
        //NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String ruleId = "allocation_rule:" + Integer.toString(new Random().nextInt());
            String sql = "INSERT INTO allocation_rule VALUES (?)";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, ruleId);
            statement.executeUpdate();
            statement = db.prepareStatement("UPDATE course SET allocation_rule_id = ? WHERE id = ?");
            statement.setString(1, ruleId);
            statement.setString(2, this.id);
            statement.executeUpdate();
            this.allocationRuleId = ruleId;
            service.getJournal().record(
                ApplicationService.ACTION_TYPE_COURSE_ALLOCATION_RULE_CREATED,
                this.id, HubObject.getId(agent), ruleId);
            db.commit();
            db.setAutoCommit(true);
            return service.getAllocationRule(allocationRuleId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Legt eine Bewerbung auf einen publizierten Studiengang an.
     *
     * @param userId ID des Bewerbers
     * @param agent ausführender Benutzer
     * @return angelegte Bewerbung
     */
    public Application apply(String userId, User agent) {
        if (!service.getCourse(id).isPublished()) {
            throw new HubObjectIllegalStateException(getId());
        }
        // NOTE Race Condition: SELECT-INSERT
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
            Application application = this.service.getApplication(applicationId);

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
                statement.setString(4, null);
                statement.setObject(5, null);
                statement.setString(6, Evaluation.STATUS_INFORMATION_MISSING);
                statement.executeUpdate();
            }

            // Vorhandene Informationen der Bewerbung zuordnen
            // NOTE: Query kann noch optimiert werden
            List<Information> informationSet =
                this.service.getUser(userId).getInformationSet(null);
            for (Information information : informationSet) {
                application.assignInformation(information);
            }

            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_APPLIED,
                this.id, HubObject.getId(agent), applicationId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
            return application;

        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Liste aller Bewerbungen, die für diesen Studiengang abgegeben wurden.
     */
    public List<Application> getApplications() {
        try {
            List<Application> applications = new ArrayList<Application>();
            String sql = "SELECT * FROM application WHERE course_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                HashMap<String, Object> args = new HashMap<String, Object>();
                args.put("id", results.getString("id"));
                args.put("user_id", results.getString("user_id"));
                args.put("course_id", results.getString("course_id"));
                args.put("status", results.getString("status"));
                args.put("service", this.getService());
                applications.add(new Application(args));
            }
            return applications;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Publiziert den Studiengang.
     */
    public void publish(User agent) {
        AllocationRule allocationRule = getAllocationRule();
        if (allocationRule == null || allocationRule.getQuota() == null) {
            throw new HubObjectIllegalStateException(getId());
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String sql = "UPDATE course SET published = TRUE WHERE id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, getId());
            statement.executeUpdate();
            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_PUBLISHED,
                this.id, HubObject.getId(agent), null);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        published = true;
    }

    /**
     * Zieht die Publikation zurück. Kann nur erfolgen, wenn noch keine Bewerbungen auf
     * diesen Studiengang vorliegen.
     */
    public void unpublish(User agent) {
        if (!getApplications().isEmpty()) {
            throw new HubObjectIllegalStateException(getId());
        }
        try {
            Connection db = service.getDb();
            // NOTE Race Condition: SELECT-UPDATE
            db.setAutoCommit(false);
            String sql = "UPDATE course SET published = FALSE WHERE id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, getId());
            statement.executeUpdate();
            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_UNPUBLISHED,
                this.id, HubObject.getId(agent), null);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        published = false;
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

    /**
     * Publikationsstatus des Studiengangs.
     */
    public boolean isPublished() {
        return published;
    }


}
