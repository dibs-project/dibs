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

    /** Unterstützte Filter für {@link #getEvaluations(Map, User)}. */
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
            String sql = String.format("SELECT * FROM evaluation %s", filterSql);
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            Object[] params = new Object[filterValues.size()];
            for (int i = 0; i < filterValues.size(); i++) {
                params[i] = filterValues.get(i);
            }
            queryResults = service.getQueryRunner().query(service.getDb(), sql,
                new MapListHandler(), params);
            for (Map<String, Object> map : queryResults) {
                map.put("service", this.getService());
                evaluations.add(new Evaluation(map));
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
     * Zeitpunkt der letzten Modifikation der Bewerbung.
     */
    public Date getModificationTime() {
        return modificationTime;
    }

    /**
     * Version der Bewerbung im System des DoSV.
     */
    public int getDosvVersion() {
        return dosvVersion;
    }
}
