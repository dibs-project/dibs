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

import static java.util.Collections.nCopies;

import university.dibs.dibs.DibsException.IllegalStateException;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.IOError;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Bewerbung, mit der Benutzer am Zulassungsverfahren teilnehmen.
 *
 * @author Markus Michler
 * @author Sven Pfaller
 */
public class Application extends DibsObject {

    // Konstanten für den Bewerbungsstatus
    /** Incomplete: the initial status of an application. */
    public static final String STATUS_INCOMPLETE = "incomplete";
    /** Complete: the applicant has added all information necessary for evaluating the relevant
     * criteria.*/
    public static final String STATUS_COMPLETE = "complete";
    /** Valid: the status needed for applications to take part in the admission process. */
    public static final String STATUS_VALID = "valid";
    /** Withdrawn: The applicant has decided not to pursue this application any further. */
    public static final String STATUS_WITHDRAWN = "withdrawn";
    /** Admitted: The applicant is offered a an enrollment in the course they applied to. */
    public static final String STATUS_ADMITTED = "admitted";
    // TODO change to "accepted"?
    /** Confimed: The applicant has confirmed their intent to enroll. */
    public static final String STATUS_CONFIRMED = "confirmed";

    /** Supported filters for {@link #getEvaluations(Map, User)}. */
    public static final Set<String> GET_EVALUATIONS_FILTER_KEYS =
        ApplicationService.GET_CRITERIA_FILTER_KEYS;

    private final String userId;
    private final String courseId;
    private String status;
    private Date modificationTime;
    private final int dosvVersion;

    /**
     * Initializes Application.
     *
     * @param args TODO
     */
    Application(Map<String, Object> args) {
        super(args);
        this.userId = (String) args.get("user_id");
        this.courseId = (String) args.get("course_id");
        this.status = (String) args.get("status");
        this.modificationTime =
            new Date(((Timestamp) args.get("modification_time")).getTime());
        this.dosvVersion = (int) args.get("dosv_version");
    }

    /**
     * Accepts an admission for this Application.
     *
     * @throws IllegalStateException if the status is not <code>"admitted"</code>.
     *     (code: <code>"application_not_admitted"</code>)
     */
    public void accept() {
        if (!this.getStatus().equals(STATUS_ADMITTED)) {
            throw new IllegalStateException("application_not_admitted");
        }
        // TODO IllegalStateException("application_is_dosv") if the
        // admission can only be accepted via Hochschulstart.de once Course.startAdmission()
        // is testable for DoSV courses.
        this.setStatus(STATUS_CONFIRMED, true, null);
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
     * All evaluations belonging to this application.
     *
     * @see #getEvaluations(Map, User)
     */
    // overload
    public List<Evaluation> getEvaluations(User agent) {
        return this.getEvaluations(new HashMap<String, Object>(), agent);
    }

    /**
     * All evaluations belonging to this application.
     * @param filter TODO
     * @param agent active user
     * @return TODO
     */
    public List<Evaluation> getEvaluations(Map<String, Object> filter, User agent) {
        if (!GET_EVALUATIONS_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("filter_unknown_keys");
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

    // TODO replace doCommit with transaction
    void setStatus(String status, boolean doCommit, User agent) {
        Date now = new Date();
        try {
            this.service.getDb().setAutoCommit(false);
            this.service.getQueryRunner().update(service.getDb(),
                "UPDATE application SET status = ?, modification_time = ? WHERE id = ?",
                status, new Timestamp(now.getTime()), this.id);
            this.service.getJournal().record(ApplicationService.ACTION_TYPE_APPLICATION_STATUS_SET,
                this.id, DibsObject.getId(agent), status);
            if (doCommit) {
                this.service.getDb().commit();
                this.service.getDb().setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IOError(e);
        }
        this.status = status;
        this.modificationTime = now;
    }

    void assignInformation(Information information) {
        // ordnet eine Information der Bewerbung (bzw. den entsprechenden Bewertungen) zu
        Map<String, Object> filter = new HashMap<String, Object>();
        filter.put("required_information_type_id", information.getType().getId());
        for (Evaluation evaluation : this.getEvaluations(filter, null)) {
            evaluation.assignInformation(information);
        }

        // TODO will be replaced by update method / (pseudo) event
        this.setStatus(STATUS_VALID, false, null);
    }

    void userInformationCreated(User user, Information information) {
        this.assignInformation(information);
    }

    /* ---- Properties ---- */

    /**
     * Benutzer, zu dem die Bewerbung gehört.
     */
    public User getUser() {
        return this.service.getUser(this.userId);
    }

    /**
     * Studiengang, auf den der Benutzer sich beworben hat.
     */
    public Course getCourse() {
        return this.service.getCourse(this.courseId);
    }

    /**
     * Status der Bewerbung.<br>
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
        return this.status;
    }

    /**
     * Time of the Application's last modification.
     */
    public Date getModificationTime() {
        return new Date(this.modificationTime.getTime());
    }

    /**
     * Application version in the DoSV system.
     */
    public int getDosvVersion() {
        return this.dosvVersion;
    }

    /* ---- /Properties ---- */
}
