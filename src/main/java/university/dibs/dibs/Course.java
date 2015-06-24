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

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.IOError;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * University course which users apply for.
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class Course extends DibsObject {
    private String name;
    private int capacity;
    private String allocationRuleId;
    private boolean published;
    private boolean admission;
    private Date modificationTime;
    private final boolean dosv;

    Course(Map<String, Object> args) {
        super(args);
        this.name = (String) args.get("name");
        this.capacity = (Integer) args.get("capacity");
        this.allocationRuleId = (String) args.get("allocation_rule_id");
        this.published = (Boolean) args.get("published");
        this.admission = (Boolean) args.get("admission");
        this.modificationTime = new Date(((Timestamp) args.get("modification_time")).getTime());
        this.dosv = (Boolean) args.get("dosv");
    }

    /**
     * Legt eine neue Vergaberegel an und verknüpft diese mit dem Studiengang.
     *
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Vergaberegel
     */
    public AllocationRule createAllocationRule(User agent) {
        if (service.getCourse(id).isPublished()) {
            throw new IllegalStateException("course_published");
        }
        //NOTE Race Condition: SELECT-UPDATE
        try {
            this.service.beginTransaction();
            String ruleId = "allocation_rule:" + Integer.toString(new Random().nextInt());
            this.service.getQueryRunner().insert(this.service.getDb(),
                "INSERT INTO allocation_rule VALUES (?)", new MapHandler(), ruleId);
            this.service.getQueryRunner().update(this.service.getDb(),
                "UPDATE course SET allocation_rule_id = ? WHERE id = ?", ruleId, this.id);
            this.allocationRuleId = ruleId;
            this.service.getJournal().record(
                ApplicationService.ACTION_TYPE_COURSE_ALLOCATION_RULE_CREATED,
                this.id, DibsObject.getId(agent), ruleId);
            this.service.endTransaction();
            return this.service.getAllocationRule(this.allocationRuleId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Creates an Application for a published Course.
     *
     * @param userId the applicant's ID
     * @param agent executing user
     * @return created Application
     *
     * @throws DibsException.IllegalStateException
     *  if the course is not published (<code>course_not_published</code>),
     *      the course is in admission (<code>course_in_admission</code>)
     *      or the user is not connected to the DoSV (<code>user_not_connected</code>)
     */
    public Application apply(String userId, User agent) {
        if (!this.service.getCourse(id).isPublished()) {
            throw new IllegalStateException("course_not_published");
        }
        if (this.service.getCourse(id).isAdmission()) {
            throw new IllegalStateException("course_in_admission");
        }
        if (this.dosv && this.service.getUser(userId).getDosvBid() == null) {
            throw new IllegalStateException("user_not_connected");
        }
        // NOTE Race Condition: SELECT-INSERT
        try {
            this.service.beginTransaction();
            String applicationId =
                String.format("application:%s", new Random().nextInt());
            this.service.getQueryRunner().insert(this.service.getDb(),
                "INSERT INTO application VALUES (?, ?, ?, ?)", new MapHandler(),
                applicationId, userId, this.id, Application.STATUS_INCOMPLETE);
            Application application = this.service.getApplication(applicationId);

            // Bewertung für jedes Kriterium der Verteilungsregel erstellen
            // NOTE: Query kann noch optimiert werden
            List<Criterion> criteria =
                this.getAllocationRule().getQuota().getRankingCriteria();
            for (Criterion criterion : criteria) {
                String id = String.format("evaluation:%s", new Random().nextInt());
                this.service.getQueryRunner().insert(this.service.getDb(),
                    "INSERT INTO evaluation VALUES (?, ?, ?, ?, ?, ?)", new MapHandler(),
                    id, applicationId, criterion.getId(), null,
                    null, Evaluation.STATUS_INFORMATION_MISSING);
            }

            // Vorhandene Informationen der Bewerbung zuordnen
            // NOTE: Query kann noch optimiert werden
            List<Information> informationSet =
                this.service.getUser(userId).getInformationSet(null);
            for (Information information : informationSet) {
                application.assignInformation(information);
            }

            this.service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_APPLIED,
                this.id, DibsObject.getId(agent), applicationId);
            this.service.endTransaction();
            return application;

        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Generiert die Rangliste für den Studiengang.
     */
    public void generateRankings() {
        this.getAllocationRule().getQuota().generateRanking();
    }

    /**
     * Publiziert den Studiengang.
     *
     * @param agent TODO
     */
    public void publish(User agent) {
        Date now = new Date();
        AllocationRule allocationRule = this.getAllocationRule();
        if (allocationRule == null || allocationRule.getQuota() == null) {
            throw new IllegalStateException("course_incomplete");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            this.service.beginTransaction();
            this.service.getQueryRunner().update(this.service.getDb(),
                "UPDATE course SET published = TRUE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            this.service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_PUBLISHED,
                this.id, DibsObject.getId(agent), null);
            this.service.endTransaction();
        } catch (SQLException e) {
            throw new IOError(e);
        }
        this.published = true;
        this.modificationTime = now;
    }

    /**
     * Unpublishes the course.
     *
     * @param agent TODO
     * @throws DibsException.IllegalStateException if an {@link Application} for this
     *     course exists (<code>course_has_applications</code>)
     */
    public void unpublish(User agent) {
        Date now = new Date();
        // NOTE Bewerbungsabfrage kann noch optimiert werden
        if (!this.getApplications().isEmpty()) {
            throw new IllegalStateException("course_has_applications");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            this.service.beginTransaction();
            this.service.getQueryRunner().update(this.service.getDb(),
                "UPDATE course SET published = FALSE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            this.service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_UNPUBLISHED,
                this.id, DibsObject.getId(agent), null);
            this.service.endTransaction();
        } catch (SQLException e) {
            throw new IOError(e);
        }
        this.published = false;
        this.modificationTime = now;
    }

    /**
     * Starts the admission phase.
     *
     * @param agent TODO
     */
    public void startAdmission(User agent) {
        Date now = new Date();
        if (!this.isPublished()) {
            throw new IllegalStateException("course_unpublished");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            this.service.beginTransaction();
            this.service.getQueryRunner().update(this.service.getDb(),
                "UPDATE course SET admission = TRUE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            this.service.getJournal().record(
                ApplicationService.ACTION_TYPE_COURSE_ADMISSION_STARTED, this.id,
                DibsObject.getId(agent), null);

            // NOTE future iterations: will be called when a user's application status is set
            this.generateRankings();

            // first local admission step
            if (!this.dosv) {
                for (Rank rank : this.getAllocationRule().getQuota().getRanking()) {
                    rank.getApplication().setStatus(Application.STATUS_ADMITTED, null);
                }
            }

            this.service.endTransaction();
        } catch (SQLException e) {
            throw new IOError(e);
        }
        this.admission = true;
        this.modificationTime = now;
    }

    /* ---- Properties ---- */

    /**
     * Name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Capacity, i.e. number of places.
     */
    public int getCapacity() {
        return this.capacity;
    }

    /**
     * Vergaberegel des Studiengangs.
     */
    public AllocationRule getAllocationRule() {
        return this.allocationRuleId != null ? this.service.getAllocationRule(this.allocationRuleId)
            : null;
    }

    /**
     * Liste aller Bewerbungen, die für diesen Studiengang abgegeben wurden.
     */
    public List<Application> getApplications() {
        try {
            List<Application> applications = new ArrayList<Application>();
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            queryResults = service.getQueryRunner().query(service.getDb(),
                "SELECT * FROM application WHERE course_id = ?",
                new MapListHandler(), id);
            for (Map<String, Object> args : queryResults) {
                args.put("service", this.getService());
                applications.add(new Application(args));
            }
            return applications;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Ruft die Rangliste für den Studiengang ab.
     *
     * @return Rangliste
     */
    public List<Rank> getRankings() {
        ArrayList<Rank> ranking = new ArrayList<Rank>();
        try {
            List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
            queryResults = this.service.getQueryRunner().query(this.service.getDb(),
                "SELECT * FROM rank WHERE quota_id = ?", new MapListHandler(),
                this.getAllocationRule().getQuota().getId());
            for (Map<String, Object> args : queryResults) {
                args.put("service", this.getService());
                ranking.add(new Rank(args));
            }
            return ranking;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Publikationsstatus des Studiengangs.
     */
    public boolean isPublished() {
        return this.published;
    }

    /**
     * Denotes whether the Course is in the admission phase.
     */
    public boolean isAdmission() {
        return this.admission;
    }

    /**
     * Zeitpunkt der letzten Modifikation des Studiengangs.
     */
    public Date getModificationTime() {
        return new Date(this.modificationTime.getTime());
    }

    /**
     * Determines whether this Course is using the DoSV for the admission process.
     */
    public boolean isDosv() {
        return this.dosv;
    }

    /* ---- /Properties ---- */
}
