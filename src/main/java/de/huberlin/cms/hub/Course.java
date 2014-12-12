/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import de.huberlin.cms.hub.HubException.IllegalStateException;

/**
 * University course which users apply for.
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class Course extends HubObject {
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
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String ruleId = "allocation_rule:" + Integer.toString(new Random().nextInt());
            service.getQueryRunner().insert(service.getDb(), "INSERT INTO allocation_rule VALUES (?)",
                new MapHandler(), ruleId);
            service.getQueryRunner().update(service.getDb(),
                "UPDATE course SET allocation_rule_id = ? WHERE id = ?", ruleId, this.id);
            this.allocationRuleId = ruleId;
            service.getJournal().record(
                ApplicationService.ACTION_TYPE_COURSE_ALLOCATION_RULE_CREATED,
                this.id, HubObject.getId(agent), ruleId);
            db.commit();
            db.setAutoCommit(true);
            return service.getAllocationRule(allocationRuleId);
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
     * @throws HubException.IllegalStateException
     *  if the course is not published (<code>course_not_published</code>),
     *  the course is in admission (<code>course_in_admission</code>)
     *  or the user is not connected to the DoSV (<code>user_not_connected</code>)
     */
    public Application apply(String userId, User agent) {
        if (!service.getCourse(id).isPublished()) {
            throw new IllegalStateException("course_not_published");
        }
        if (service.getCourse(id).isAdmission()) {
            throw new IllegalStateException("course_in_admission");
        }
        if (dosv == true && service.getUser(userId).getDosvBid() == null) {
            throw new IllegalStateException("user_not_connected");
        }
        // NOTE Race Condition: SELECT-INSERT
        try {
            service.getDb().setAutoCommit(false);
            String applicationId =
                String.format("application:%s", new Random().nextInt());
            service.getQueryRunner().insert(service.getDb(),
                "INSERT INTO application VALUES (?, ?, ?, ?)", new MapHandler(),
                applicationId, userId, this.id, Application.STATUS_INCOMPLETE);
            Application application = this.service.getApplication(applicationId);

            // Bewertung für jedes Kriterium der Verteilungsregel erstellen
            // NOTE: Query kann noch optimiert werden
            List<Criterion> criteria =
                this.getAllocationRule().getQuota().getRankingCriteria();
            for (Criterion criterion : criteria) {
                String id = String.format("evaluation:%s", new Random().nextInt());
                service.getQueryRunner().insert(service.getDb(),
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

            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_APPLIED,
                this.id, HubObject.getId(agent), applicationId);
            service.getDb().commit();
            service.getDb().setAutoCommit(true);
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
     */
    public void publish(User agent) {
        Date now = new Date();
        AllocationRule allocationRule = getAllocationRule();
        if (allocationRule == null || allocationRule.getQuota() == null) {
            throw new IllegalStateException("course_incomplete");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            service.getQueryRunner().update(service.getDb(),
                "UPDATE course SET published = TRUE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_PUBLISHED,
                this.id, HubObject.getId(agent), null);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        published = true;
        modificationTime = now;
    }

    /**
     * Unpublishes the course.
     *
     * @throws HubException.IllegalStateException if an {@link Application} for this
     *     course exists (<code>course_has_applications</code>)
     */
    public void unpublish(User agent) {
        Date now = new Date();
        // NOTE Bewerbungsabfrage kann noch optimiert werden
        if (!getApplications().isEmpty()) {
            throw new IllegalStateException("course_has_applications");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            service.getQueryRunner().update(service.getDb(),
                "UPDATE course SET published = FALSE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            service.getJournal().record(ApplicationService.ACTION_TYPE_COURSE_UNPUBLISHED,
                this.id, HubObject.getId(agent), null);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        published = false;
        modificationTime = now;
    }

    /**
     * Starts the admission phase.
     */
    public void startAdmission(User agent) {
        Date now = new Date();
        if (!isPublished()) {
            throw new IllegalStateException("course_unpublished");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            service.getQueryRunner().update(service.getDb(),
                "UPDATE course SET admission = TRUE, modification_time = ? WHERE id = ?",
                new Timestamp(now.getTime()), getId());
            service.getJournal().record(
                ApplicationService.ACTION_TYPE_COURSE_ADMISSION_STARTED, this.id,
                HubObject.getId(agent), null);

            // NOTE future iterations: will be called when a user's application status is set
            generateRankings();

            // first local admission step
            if (!dosv) {
                for (Rank rank : getAllocationRule().getQuota().getRanking()) {
                    rank.getApplication().setStatus(Application.STATUS_ADMITTED, false, null);
                }
            }

            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
        admission = true;
        modificationTime = now;
    }

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
        return allocationRuleId != null ? service.getAllocationRule(allocationRuleId) : null;
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
            queryResults = service.getQueryRunner().query(service.getDb(),
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
        return published;
    }

    /**
     * Denotes whether the Course is in the admission phase.
     */
    public boolean isAdmission() {
        return admission;
    }

    /**
     * Zeitpunkt der letzten Modifikation des Studiengangs.
     */
    public Date getModificationTime() {
        return new Date(modificationTime.getTime());
    }

    /**
     * Determines whether this Course is using the DoSV for the admission process.
     */
    public boolean isDosv() {
        return dosv;
    }
}
