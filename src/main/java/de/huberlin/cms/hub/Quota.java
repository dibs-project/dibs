/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Quote, welche die Kriterien für die Ranglistenerstellung für eine Untermenge der Plätze
 * eines Studienangebots beinhaltet.
 *
 * @author Markus Michler
 */
public class Quota extends HubObject {
    private String name;
    private double percentage;

    Quota(HashMap<String, Object> args) {
        super((String)args.get("id"), (ApplicationService)args.get("service"));
        this.name = (String)args.get("name");
        this.percentage = (Double)args.get("percentage");
    }

    void addRankingCriteria(List<Criterion> rankingCriteria) {
        for (Criterion criterion : rankingCriteria) {
            try {
                String sql = "INSERT INTO quota_ranking_criteria VALUES(?, ?)";
                PreparedStatement statement = service.getDb().prepareStatement(sql);
                statement.setString(1, id);
                statement.setString(2, criterion.getId());
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }

    /**
     * Kriterien für die Sortierung der Bewerber auf der Rangliste
     */
    public List<Criterion> getRankingCriteria() {
        List<Criterion> rankingCriteria = new ArrayList<Criterion>();
        ResultSet results;
        try {
            String query =
                "SELECT criterion_id FROM quota_ranking_criteria WHERE quota_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(query);
            statement.setString(1, id);
            results = statement.executeQuery();
            while (results.next()) {
                rankingCriteria.add(service.getCriteria().get(
                    results.getString("criterion_id")));
            }
        } catch (SQLException e) {
            throw new IOError(e);
        }
        return rankingCriteria;
    }

    // TODO Kriterien für die Aufnahme von Bewerbungen in die Quote:
    // List<Criterion> getInclusionCriteria()

    /**
     * Name der Quote
     */
    public String getName() {
        return name;
    }

    /**
     * Prozentualer Anteil der Quote an der Gesamtzahl der vergebenen Studienplätze.
     */
    public double getPercentage() {
        return percentage;
    }
}
