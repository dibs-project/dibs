/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

// TODO: document
public class Session extends HubObject {
    private final String userId;
    private final String device;
    private final Date startTime;
    private Date endTime;

    Session(Map<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.userId = (String) args.get("user_id");
        this.device = (String) args.get("device");
        this.startTime = (Date) args.get("start_time");
        this.endTime = (Date) args.get("end_time");
    }

    // TODO: document
    public void end() {
        try {
            Timestamp now = new Timestamp(new Date().getTime());
            PreparedStatement statement = this.getService().getDb().prepareStatement(
                "UPDATE session SET end_time = ? WHERE id = ? AND end_time > ?");
            statement.setTimestamp(1, now);
            statement.setString(2, this.getId());
            statement.setTimestamp(3, now);
            if (statement.executeUpdate() == 1) {
                this.endTime = now;
            }
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    // TODO: document
    public User getUser() {
        return this.service.getUser(this.userId);
    }

    // TODO: document
    public String getDevice() {
        return this.device;
    }

    // TODO: document
    public Date getStartTime() {
        return this.startTime;
    }

    // TODO: document
    public Date getEndTime() {
        return this.endTime;
    }

    // TODO: document
    public boolean isValid() {
        return this.endTime.after(new Date());
    }
}
