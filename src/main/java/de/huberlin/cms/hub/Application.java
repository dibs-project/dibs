/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static java.util.Collections.nCopies;

import java.io.IOError;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 * @author Sven Pfaller
 */
public class Application extends HubObject {

    // Konstanten für den Bewerbungsstatus
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_VALID = "valid";
    public static final String STATUS_WITHDRAWN = "withdrawn";
    public static final String STATUS_ADMITTED = "admitted";
    public static final String STATUS_CONFIRMED = "confirmed";

    /** Supported filters for {@link #getEvaluations(Map, User)}. */
    public static final Set<String> GET_EVALUATIONS_FILTER_KEYS =
        ApplicationService.GET_CRITERIA_FILTER_KEYS;

    private final String userId;
    private final String courseId;
    private String status;
    private Date modificationTime;
    private final int dosvVersion;

    Application(Map<String, Object> args) {
        super(args);
        this.userId = (String) args.get("user_id");
        this.courseId = (String) args.get("course_id");
        this.status = (String) args.get("status");
        this.modificationTime = (Date) args.get("modification_time");
        this.dosvVersion = (int) args.get("dosv_version");
    }

    /**
     * Gibt die Bewertung zurück, die sich auf das angegebene Kriterium bezieht.
     *
     * @param criterionId ID des Kriteriums
     * @return Bewertung, die sich auf das angegebene Kriterium bezieht
     */
    public Evaluation getEvaluationByCriterionId(String criterionId) {
        try {
            Map<String, Object> args = service.getQueryRunner().query(service.getDb(),
                "SELECT * FROM evaluation WHERE application_id = ? AND criterion_id = ?",
                new MapHandler(), this.id, criterionId);
            if (args == null) {
                throw new IllegalArgumentException(
                    "illegal criterionId: evaluation does not exist");
            }
            args.put("service", service);
            return new Evaluation(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Returns a list of all evaluations belonging to this application.
     *
     * @param filter filter (errors: <code>filter_improper_keys</code>)
     * @param agent active user
     * @return list of all evaluations belonging to this application
     */
    public List<Evaluation> getEvaluations(Map<String, Object> filter, User agent) {
        if (!GET_EVALUATIONS_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("filter_improper_keys");
        }

        ArrayList<String> filterConditions = new ArrayList<>();
        ArrayList<Object> filterValues = new ArrayList<>();
        filterConditions.add("application_id = ?");
        filterValues.add(this.id);
        if (filter.containsKey("required_information_type_id")) {
            List<Criterion> criteria = this.service.getCriteria(filter, agent);
            filterConditions.add(String.format("criterion_id IN (%s)",
                StringUtils.join(nCopies(criteria.size(), "?"), ", ")));
            for (Criterion criterion : criteria) {
                filterValues.add(criterion.getId());
            }
        }
        String filterSql = " WHERE " + StringUtils.join(filterConditions, " AND ");

        try {
            // NOTE: optimized query
            String sql = "SELECT * FROM evaluation" + filterSql;
            List<Map<String, Object>> results = service.getQueryRunner().query(
                this.service.getDb(), sql, new MapListHandler(), filterValues.toArray());

            ArrayList<Evaluation> evaluations = new ArrayList<>();
            for (Map<String, Object> args : results) {
                args.put("service", this.service);
                evaluations.add(new Evaluation(args));
            }
            return evaluations;

        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Returns a list of all evaluations belonging to this application.
     *
     * @see #getEvaluations(Map, User)
     */
    public List<Evaluation> getEvaluations(User agent) {
        return this.getEvaluations(new HashMap<String, Object>(), agent);
    }

    void setStatus(String status, User agent) {
        Date now = new Date();
        try {
            service.getDb().setAutoCommit(false);
            service.getQueryRunner().update(service.getDb(),
                "UPDATE application SET status = ?, modification_time = ? WHERE id = ?",
                status, new Timestamp(now.getTime()), this.id);
            service.getJournal().record(ApplicationService.ACTION_TYPE_APPLICATION_STATUS_SET,
                this.id, HubObject.getId(agent), status);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        this.status = status;
        modificationTime = now;
    }

    void assignInformation(Information information) {
        // ordnet eine Information der Bewerbung (bzw. den entsprechenden Bewertungen) zu
        HashMap<String, Object> filter = new HashMap<String, Object>();
        filter.put("required_information_type_id", information.getType().getId());
        for (Evaluation evaluation : this.getEvaluations(filter, null)) {
            evaluation.assignInformation(information);
        }
    }

    void userInformationCreated(User user, Information information) {
        this.assignInformation(information);
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

    /**
     * Time of the Application's last modification.
     */
    public Date getModificationTime() {
        return new Date(modificationTime.getTime());
    }

    /**
     * Application version in the DoSV system.
     */
    public int getDosvVersion() {
        return dosvVersion;
    }
}
