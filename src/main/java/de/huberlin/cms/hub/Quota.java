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

/**
 * Quote, welche die Kriterien für die Ranglistenerstellung für einen Teil der Plätze
 * eines Studiengangs beinhaltet.
 *
 * @author Markus Michler
 */
public class Quota extends HubObject {
    /** Aktionstyp: Kriterium zur Sortierung von Bewerbern mit Quote verknüpft. */
    public static final String ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED =
        "quota_ranking_criterion_added";
    private final String name;
    private final int percentage;

    Quota(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
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
        Connection db = service.getDb();
        try {
            db.setAutoCommit(false);
            String sql = "INSERT INTO quota_ranking_criteria VALUES(?, ?)";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, criterionId);
            statement.executeUpdate();
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
            String query = "SELECT criterion_id FROM quota_ranking_criteria WHERE quota_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(query);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                rankingCriteria.add(service.getCriteria().get(
                    results.getString("criterion_id")));
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
}
