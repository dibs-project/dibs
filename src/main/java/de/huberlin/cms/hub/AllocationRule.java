/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static de.huberlin.cms.hub.Util.isInRange;

import java.io.IOError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import de.huberlin.cms.hub.HubException.HubObjectIllegalStateException;

/**
 * Regel, nach der Studienplätze für einen Studiengang an die Bewerber vergeben werden
 * (Vergabeschema).
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class AllocationRule extends HubObject {
    private String quotaId;

    AllocationRule(String id, ApplicationService service) {
        super(id, service);
    }

    AllocationRule(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert das Vergabeschema über den Datenbankcursor
        this(results.getString("id"), service);
        this.quotaId = results.getString("quota_id");
    }

    /**
     * Erstellt und verknüpft eine Quote.
     *
     * @param name Name der Quote, darf nicht leer sein
     * @param percentage Anteil der Quote an der Gesamtallokation in Prozent
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Quote
     */
    public Quota createQuota(String name, int percentage, User agent) {
        if (!isInRange(percentage, 0, 100)) {
            throw new IllegalArgumentException("illegal percentage: out of range");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        Course course = getCourse();
        if (course.isPublished()) {
            throw new HubObjectIllegalStateException(course.getId());
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String quotaId = "quota:" + Integer.toString(new Random().nextInt());
            String sql = "INSERT INTO quota VALUES (?, ?, ?)";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, quotaId);
            statement.setString(2, name);
            statement.setDouble(3, percentage);
            statement.executeUpdate();
            sql = "UPDATE allocation_rule SET quota_id = ? WHERE id = ?";
            statement = db.prepareStatement(sql);
            statement.setString(1, quotaId);
            statement.setString(2, this.id);
            statement.executeUpdate();
            this.quotaId = quotaId;
            service.getJournal().record(ApplicationService.ACTION_TYPE_ALLOCATION_RULE_QUOTA_CREATED,
                this.id, HubObject.getId(agent), quotaId);
            db.commit();
            db.setAutoCommit(true);
            return service.getQuota(this.quotaId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Quote der Vergaberegel.
     */
    public Quota getQuota() {
        return quotaId != null ? service.getQuota(quotaId) : null;
    }

    /**
     * Studiengang, zu dem diese Vergaberegel gehört.
     */
    public Course getCourse() {
        try {
            String sql = "SELECT * FROM course WHERE allocation_rule_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            results.next();
            return new Course(results, service);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
