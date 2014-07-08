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

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

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
     * @param name Name der Quote, darf nicht <code>null</code> oder leer sein
     * @param percentage Anteil der Quote an der Gesamtallokation in Prozent
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Quote
     */
    public Quota createQuota(String name, double percentage, User agent) {
        if (!isInRange(percentage, (double) 0, (double) 100)) {
            throw new IllegalArgumentException("illegal percentage: out of range");
        }
        if (name == null) {
            throw new IllegalArgumentException("illegal name: can not be null");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
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
            service.getJournal().record(ActionType.ALLOCATION_RULE_QUOTA_CREATED,
                ObjectType.ALLOCATION_RULE, this.id, HubObject.getId(agent), quotaId);
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
}
