/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static java.util.Collections.nCopies;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 */
public class Application extends HubObject {

    // Konstanten für den Bewerbungsstatus
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_VALID = "valid";
    public static final String STATUS_WITHDRAWN = "withdrawn";
    public static final String STATUS_ADMITTED = "admitted";
    public static final String STATUS_CONFIRMED = "confirmed";

    /** Unterstützte Filter für {@link #getEvaluations(Map, User)}. */
    public static final Set<String> GET_EVALUATIONS_FILTER_KEYS =
        ApplicationService.GET_CRITERIA_FILTER_KEYS;

    private final String userId;
    private final String courseId;
    private String status;

    Application(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.userId = (String) args.get("user_id");
        this.courseId = (String) args.get("course_id");
        this.status = (String) args.get("status");
    }

    /**
     * Gibt eine Liste aller Bewertungen, die zu dieser Bewerbung gehören, zurück.
     *
     * @param filter Filter
     * @param agent ausführender Benutzer
     * @return Liste aller Bewertungen, die zu dieser Bewerbung gehören
     */
    public List<Evaluation> getEvaluations(Map<String, Object> filter, User agent) {
        if (!GET_EVALUATIONS_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("illegal filter: improper keys");
        }

        // Filter zusammensetzen
        String filterSql = "WHERE application_id = ?";
        ArrayList<Object> filterValues = new ArrayList<Object>();
        filterValues.add(this.id);
        if (filter.containsKey("required_information_type_id")) {
            List<Criterion> criteria = this.service.getCriteria(filter, agent);
            filterSql += String.format(" AND criterion_id IN (%s)",
                StringUtils.join(nCopies(criteria.size(), "?"), ", "));
            for (Criterion criterion : criteria) {
                filterValues.add(criterion.getId());
            }
        }

        try {
            ArrayList<Evaluation> evaluations = new ArrayList<Evaluation>();
            // NOTE: optimierter Query
            PreparedStatement statement = this.service.getDb().prepareStatement(
                String.format("SELECT * FROM evaluation %s", filterSql));
            for (int i = 0; i < filterValues.size(); i++) {
                statement.setObject(i + 1, filterValues.get(i));
            }
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                HashMap<String, Object> args = new HashMap<String, Object>();
                args.put("id", results.getString("id"));
                args.put("application_id", results.getString("application_id"));
                args.put("criterion_id", results.getString("criterion_id"));
                args.put("information_id", results.getString("information_id"));
                args.put("value", results.getDouble("value"));
                args.put("status", results.getString("status"));
                args.put("service", this.service);
                evaluations.add(new Evaluation(args));
            }
            return evaluations;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt eine Liste aller Bewertungen, die zu dieser Bewerbung gehören, zurück.
     *
     * @see #getEvaluations(Map, User)
     */
    public List<Evaluation> getEvaluations(User agent) {
        return this.getEvaluations(new HashMap<String, Object>(), agent);
    }

    void setStatus(String status, User agent) {
        this.status = status;
        try {
            service.getDb().setAutoCommit(false);
            String sql = "UPDATE application SET status = ? WHERE id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, status);
            statement.setString(2, this.id);
            statement.executeUpdate();
            service.getJournal().record(JournalRecord.ACTION_TYPE_APPLICATION_STATUS_SET,
                this.id, HubObject.getId(agent), status);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Benutzer, zu dem die Bewerbung gehört.
     */
    public User getUser() {
        return service.getUser(this.userId);
    }

    /**
     * Studiengang, auf den der Benutzer sich beworben hat.
     */
    public Course getCourse() {
        return service.getCourse(this.courseId);
    }

    /**
     * Status der Bewerbung.</br>
     * Konstanten:
     * <ul>
     * <li><code>incomplete</code>: angelegt, nicht vollständig
     * <li><code>complete</code>: vom Benutzer finalisiert, vollständig
     * <li><code>valid</code>: gültig, nimmt am Zulassungsverfahren teil
     * <li><code>withdrawn</code>: zurückgezogen
     * <li><code>admitted</code>: Zulassungsangebot ausgesprochen
     * <li><code>confirmed</code>: Zulassungsangebot angenommen, zugelassen
     * </ul>
     */
    public String getStatus() {
        return status;
    }
}
