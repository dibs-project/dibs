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

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Quote, welche die Kriterien für die Ranglistenerstellung für einen Teil der Plätze
 * eines Studienangebots beinhaltet.
 *
 * @author Markus Michler
 */
public class Quota extends HubObject {
    private final String name;
    private final double percentage;

    Quota(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.name = (String) args.get("name");
        this.percentage = Double.parseDouble((String) args.get("percentage"));
    }

    /**
     * verknüpft ein Kriterium zur Sortierung von Bewerbern auf einer Rangliste.
     *
     * @param criterionId ID des zu verknüpfenden Kriteriums
     * @param agent ausführender Benutzer
     */
    public void addRankingCriterion(String criterionId, User agent) {
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String sql = "INSERT INTO quota_ranking_criteria VALUES(?, ?)";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, id);
            statement.setString(2, criterionId);
            statement.executeUpdate();
            service.getJournal().record(ActionType.QUOTA_CRITERION_CREATED,
                ObjectType.QUOTA, this.id, HubObject.getId(agent), criterionId);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Kriterien für die Sortierung der Bewerber auf der Rangliste.
     */
    public List<Criterion> getRankingCriteria() {
        List<Criterion> rankingCriteria = new ArrayList<Criterion>();
        try {
            ResultSet results;
            String query = "SELECT criterion_id FROM quota_ranking_criteria WHERE quota_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(query);
            statement.setString(1, id);
            results = statement.executeQuery();
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
     * Prozentualer Anteil der Quote an der Gesamtzahl der vergebenen Studienplätze.
     */
    public double getPercentage() {
        return percentage;
    }
}
