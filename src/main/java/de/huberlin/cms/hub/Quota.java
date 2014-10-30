/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.huberlin.cms.hub.HubException.IllegalStateException;

/**
 * Quote, welche die Kriterien für die Ranglistenerstellung für einen Teil der Plätze
 * eines Studiengangs beinhaltet.
 *
 * @author Markus Michler
 */
public class Quota extends HubObject {
    private final String name;
    private final int percentage;

    Quota(Map<String, Object> args) {
        super(args);
        this.name = (String) args.get("name");
        this.percentage = (Integer) args.get("percentage");
    }

    /**
     * Verknüpft ein Kriterium zur Sortierung von Bewerbern auf einer Rangliste.
     *
     * @param criterionId ID des zu verknüpfenden Kriteriums
     * @param agent ausführender Benutzer
     */
    public void addRankingCriterion(String criterionId, User agent) {
        if (getAllocationRule().getCourse().isPublished()) {
            throw new IllegalStateException("course_published");
        }
        // NOTE Race Condition: SELECT-INSERT
        Connection db = service.getDb();
        try {
            db.setAutoCommit(false);
            service.getQueryRunner().insert(service.getDb(), "INSERT INTO quota_ranking_criteria VALUES(?, ?)",
                service.getMapHandler(), id, criterionId);
            service.getJournal().record(ApplicationService.ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED,
                this.id, HubObject.getId(agent), criterionId);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                // unique violation ignorieren
                try {
                    db.rollback();
                    db.setAutoCommit(true);
                } catch (SQLException e1) {
                    throw new IOError(e1);
                }
            } else {
                throw new IOError(e);
            }
        }
    }

    /**
     * Kriterien für die Sortierung der Bewerber auf der Rangliste.
     */
    public List<Criterion> getRankingCriteria() {
        List<Criterion> rankingCriteria = new ArrayList<Criterion>();
        try {
            String sql = "SELECT criterion_id FROM quota_ranking_criteria WHERE quota_id = ?";
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            queryResults = service.getQueryRunner().query(service.getDb(), sql,
                    service.getMapListHandler(), id);
            for (Map<String, Object> args : queryResults) {
                rankingCriteria.add(service.getCriteria().get(args.get("criterion_id")));
            }
            return rankingCriteria;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    // TODO Kriterien für die Aufnahme von Bewerbungen in die Quote:
    // List<Criterion> getInclusionCriteria()

    /**
     * Name der Quote.
     */
    public String getName() {
        return name;
    }

    /**
     * Prozentualer Anteil (0..100) der Quote an der Gesamtzahl der vergebenen Studienplätze.
     */
    public int getPercentage() {
        return percentage;
    }

    /**
     * Vergaberegel, zu der diese Quote gehört.
     */
    public AllocationRule getAllocationRule() {
        try {
            Map<String, Object> args = service.getQueryRunner().query(service.getDb(),
                "SELECT * FROM allocation_rule WHERE quota_id = ?",
                service.getMapHandler(), id);
            args.put("service", service);
            return new AllocationRule(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
