/*
 * dibs
 * Copyright (C) 2015  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package university.dibs.dibs;

import university.dibs.dibs.DibsException.IllegalStateException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.IOError;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * Contains the criteria for the creation of a ranking list for a percentage of a course's
 * available places.
 *
 * @author David Koschnick
 * @author Markus Michler
 */
public class Quota extends DibsObject {
    /** Supported filters for {@link #getApplications(Map)}. */
    public static final Set<String> GET_APPLICATIONS_FILTER_KEYS =
        new HashSet<>(Arrays.asList("status"));

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
        // TODO Sobald es mehrere Kriterien gibt, muss die Funktion angepasst werden, da die
        // Reihenfolge der Kriterien nicht gewährleistet werden kann
        if (this.getAllocationRule().getCourse().isPublished()) {
            throw new IllegalStateException("course_published");
        }
        // NOTE Race Condition: SELECT-INSERT
        try {
            this.service.beginTransaction();
            this.service.getQueryRunner().insert(this.service.getDb(),
                "INSERT INTO quota_ranking_criteria VALUES(?, ?)", new MapHandler(), this.id,
                criterionId);
            this.service.getJournal().record(
                ApplicationService.ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED, this.id,
                DibsObject.getId(agent), criterionId);
            this.service.endTransaction();
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                // unique violation ignorieren
                service.endTransaction();
            } else {
                throw new IOError(e);
            }
        }
    }

    /**
     * Generiert die Rangliste für die Quote.
     *
     * @return Rangliste
     */
    public List<Rank> generateRanking() {
        Map<String, Object> filterArgs = new HashMap<>();
        filterArgs.put("status", Application.STATUS_VALID);
        List<Application> applications = this.getApplications(filterArgs);
        final Map<Application, List<Evaluation>> evaluations = this.getEvaluations();
        final Map<Application, Integer> lotnumbers = new HashMap<>();
        for (Application application : applications) {
            lotnumbers.put(application, new Random().nextInt(1000000));
        }
        Collections.sort(applications, new Comparator<Application>() {
            @Override
            public int compare(Application a1, Application a2) {
                List<Criterion> criteria = Quota.this.getRankingCriteria();
                for (final Criterion criterion : criteria) {
                    Predicate<Evaluation> predicate = new Predicate<Evaluation>() {
                        @Override
                        public boolean evaluate(Evaluation evaluation) {
                            return evaluation.getCriterion().getId().equals(criterion.id);
                        }
                    };
                    Evaluation eval1 = CollectionUtils.find(evaluations.get(a1), predicate);
                    Evaluation eval2 = CollectionUtils.find(evaluations.get(a2), predicate);
                    double value = eval1.getValue() - eval2.getValue();
                    if (value != 0) {
                        return (int) value;
                    }
                }
                return lotnumbers.get(a1) - lotnumbers.get(a2);
            }
        });
        List<Rank> ranking = new ArrayList<Rank>();
        for (int i = 0; i < applications.size(); i++) {
            Application application = applications.get(i);
            Map<String, Object> args = new HashMap<String, Object>();
            args.put("quota_id", this.getId());
            args.put("user_id", application.getUser().getId());
            args.put("application_id", application.getId());
            args.put("index", i);
            args.put("lotnumber", lotnumbers.get(application));
            args.put("service", this.service);
            ranking.add(Rank.create(args));
        }
        return ranking;
    }

    /**
     * All Applications that are included in this Quota.
     *
     * @see #getApplications(Map)
     */
    // overload
    public List<Application> getApplications() {
        return this.getApplications(new HashMap<String, Object>());
    }

    /**
     * All Applications that are included in this Quota.
     *
     * @param filter TODO
     * @return TODO
     */
    public List<Application> getApplications(Map<String, Object> filter) {
        if (!GET_APPLICATIONS_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("filter_unknown_keys");
        }

        ArrayList<String> filterConditions = new ArrayList<>();
        ArrayList<Object> filterValues = new ArrayList<>();
        filterConditions.add("allocation_rule.quota_id = ?");
        filterValues.add(this.id);

        String status = (String) filter.get("status");
        if (status != null) {
            filterConditions.add("status = ?");
            filterValues.add(status);
        }

        String filterSql = " WHERE " + StringUtils.join(filterConditions, " AND ");

        try {
            String sql = "SELECT application.* FROM application "
                    + "LEFT JOIN course ON application.course_id = course.id "
                    + "LEFT JOIN allocation_rule ON course.allocation_rule_id = allocation_rule.id "
                    + filterSql;
            List<Map<String, Object>> results = this.service.getQueryRunner().query(
                this.service.getDb(), sql, new MapListHandler(), filterValues.toArray());

            List<Application> applications = new ArrayList<>();
            for (Map<String, Object> args : results) {
                args.put("service", this.service);
                applications.add(new Application(args));
            }
            return applications;

        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /* ---- Properties ---- */

    /**
     * Ranking for this Quota.
     */
    public List<Rank> getRanking() {
        ArrayList<Rank> ranking = new ArrayList<Rank>();
        try {
            String sql = "SELECT * FROM rank WHERE quota_id = ?";
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            queryResults = this.service.getQueryRunner().query(this.service.getDb(), sql,
                new MapListHandler(), this.id);
            for (Map<String, Object> args : queryResults) {
                args.put("service", this.service);
                ranking.add(new Rank(args));
            }
            return ranking;
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
        return this.name;
    }

    /**
     * Prozentualer Anteil (0..100) der Quote an der Gesamtzahl der vergebenen Studienplätze.
     */
    public int getPercentage() {
        return this.percentage;
    }

    /**
     * Vergaberegel, zu der diese Quote gehört.
     */
    public AllocationRule getAllocationRule() {
        try {
            Map<String, Object> args = this.service.getQueryRunner().query(this.service.getDb(),
                "SELECT * FROM allocation_rule WHERE quota_id = ?",
                new MapHandler(), this.id);
            args.put("service", this.service);
            return new AllocationRule(args);
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
            String sql = "SELECT criterion_id FROM quota_ranking_criteria WHERE quota_id = ?";
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            queryResults = this.service.getQueryRunner().query(this.service.getDb(), sql,
                new MapListHandler(), this.id);
            for (Map<String, Object> args : queryResults) {
                rankingCriteria.add(this.service.getCriterion((String) args.get("criterion_id")));
            }
            return rankingCriteria;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt alle Bewerbungen mit den dazugehörigen Evaluationen
     * für die Quote des Studiengangs zurück.
     *
     * @return Map mit Bewerbungen und Liste von Evaluation
     */
    Map<Application, List<Evaluation>> getEvaluations() {
        Map<Application, List<Evaluation>> evaluationsList = new HashMap<>();
        List<Application> applications = this.getApplications();
        for (Application application : applications) {
            //TODO Performance. unperformant wegen jede abfrage einzeln
            evaluationsList.put(application, application.getEvaluations(null));
        }
        return evaluationsList;
    }

    /* ---- /Properties ---- */
}
