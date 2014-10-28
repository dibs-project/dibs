/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static de.huberlin.cms.hub.Util.isInRange;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

/**
 * Regel, nach der Studienplätze für einen Studiengang an die Bewerber vergeben werden
 * (Vergabeschema).
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class AllocationRule extends HubObject {
    private String quotaId;
    private QueryRunner queryRunner;

    AllocationRule(Map<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.quotaId = (String)args.get("quota_id");
        this.queryRunner =  new QueryRunner();
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
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String quotaId = "quota:" + Integer.toString(new Random().nextInt());
            this.queryRunner.insert(this.service.getDb(),
                "INSERT INTO quota VALUES (?, ?, ?)", new MapHandler(), quotaId, name,
                percentage);
            this.queryRunner.update(this.service.getDb(),
                "UPDATE allocation_rule SET quota_id = ? WHERE id = ?", quotaId, this.id);
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
}
