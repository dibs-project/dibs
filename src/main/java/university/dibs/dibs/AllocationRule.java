/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import static university.dibs.dibs.Util.isInRange;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.handlers.MapHandler;

import university.dibs.dibs.DibsException.IllegalStateException;

/**
 * Rule for allocating a contingent of available places to a course's applicants.
 *
 * @author Phuong Anh Ha
 * @author Markus Michler
 */
public class AllocationRule extends DibsObject {
    private String quotaId;

    AllocationRule(Map<String, Object> args) {
        super(args);
        this.quotaId = (String) args.get("quota_id");
    }

    /**
     * Erstellt und verknüpft eine Quote.
     *
     * @param name Name der Quote, darf nicht leer sein
     * @param percentage Anteil der Quote an der Gesamtallokation in Prozent
     * @param agent ausführender Benutzer
     * @return angelegte und verknüpfte Quote
     */
    public Quota createQuota(String name, int percentage, User agent) {
        if (!isInRange(percentage, 0, 100)) {
            throw new IllegalArgumentException("illegal percentage: out of range");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        Course course = getCourse();
        if (course.isPublished()) {
            throw new IllegalStateException("course_published");
        }
        // NOTE Race Condition: SELECT-UPDATE
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String quotaId = "quota:" + Integer.toString(new Random().nextInt());
            service.getQueryRunner().insert(service.getDb(), "INSERT INTO quota VALUES (?, ?, ?)",
                new MapHandler(), quotaId, name, percentage);
            service.getQueryRunner().update(this.service.getDb(),
                "UPDATE allocation_rule SET quota_id = ? WHERE id = ?", quotaId, this.id);
            this.quotaId = quotaId;
            service.getJournal().record(ApplicationService.ACTION_TYPE_ALLOCATION_RULE_QUOTA_CREATED,
                this.id, DibsObject.getId(agent), quotaId);
            db.commit();
            db.setAutoCommit(true);
            return service.getQuota(this.quotaId);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Quote der Vergaberegel.
     */
    public Quota getQuota() {
        return quotaId != null ? service.getQuota(quotaId) : null;
    }

    /**
     * Studiengang, zu dem diese Vergaberegel gehört.
     */
    public Course getCourse() {
        try {
            Map<String, Object> args = service.getQueryRunner().query(service.getDb(),
                "SELECT * FROM course WHERE allocation_rule_id = ?",
                new MapHandler(), id);
            args.put("service", service);
            return new Course(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
