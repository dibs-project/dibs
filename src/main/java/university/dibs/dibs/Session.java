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

import java.io.IOError;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;

/**
 * User session.
 *
 * @author Sven Pfaller
 */
public class Session extends DibsObject {
    private final String userId;
    private final String device;
    private final Date startTime;
    private Date endTime;

    Session(Map<String, Object> args) {
        super(args);
        this.userId = (String) args.get("user_id");
        this.device = (String) args.get("device");
        this.startTime = (Date) args.get("start_time");
        this.endTime = (Date) args.get("end_time");
    }

    /**
     * Ends the session. Sets the <code>endTime</code> to now. Does nothing if the session
     * has already ended.
     */
    public void end() {
        try {
            Timestamp now = new Timestamp(new Date().getTime());
            int count = new QueryRunner().update(this.service.getDb(),
                "UPDATE session SET end_time = ? WHERE id = ? AND end_time > ?", now,
                this.getId(), now);
            if (count == 1) {
                this.endTime = now;
            }
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * User the session belongs to.
     */
    public User getUser() {
        return this.service.getUser(this.userId);
    }

    /**
     * Device from which the session was initiated.
     */
    public String getDevice() {
        return this.device;
    }

    /**
     * Start time.
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * End time. Indicates when the session expires (future date) or when it has expired
     * (past date).
     */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * <code>true</code> if the session is valid, i.e. not yet expired, <code>false</code>
     * otherwise.
     */
    public boolean isValid() {
        return this.endTime.after(new Date());
    }
}
