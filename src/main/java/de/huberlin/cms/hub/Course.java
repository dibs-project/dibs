/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;

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
    private QueryRunner queryRunner;

    Course(Map<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.name = (String) args.get("name");
        this.capacity = (Integer) args.get("capacity");
        this.allocationRuleId = (String) args.get("allocation_rule_id");
        this.queryRunner =  new QueryRunner();
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
            this.queryRunner.insert(this.service.getDb(),
                "INSERT INTO allocation_rule VALUES (?)", new MapHandler(), ruleId);
            this.queryRunner.update(this.service.getDb(),
                "UPDATE course SET allocation_rule_id = ? WHERE id = ?", ruleId, this.id);
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
            this.queryRunner.insert(this.service.getDb(),
                "INSERT INTO application VALUES (?, ?, ?, ?)", new MapHandler(),
                applicationId, userId, this.id, Application.STATUS_INCOMPLETE);
            Application application = this.service.getApplication(applicationId);
            // Bewertung für jedes Kriterium der Verteilungsregel erstellen
            // NOTE: Query kann noch optimiert werden
            List<Criterion> criteria =
                this.getAllocationRule().getQuota().getRankingCriteria();
            for (Criterion criterion : criteria) {
                String id = String.format("evaluation:%s", new Random().nextInt());
                this.queryRunner.insert(this.service.getDb(),
                    "INSERT INTO evaluation VALUES (?, ?, ?, ?, ?, ?)", new MapHandler(),
                    id, applicationId, criterion.getId(), null, null,
                    Evaluation.STATUS_INFORMATION_MISSING);
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
