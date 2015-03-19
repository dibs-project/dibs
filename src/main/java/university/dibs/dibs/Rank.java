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
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.handlers.MapHandler;


/**
 * Rangliste für eine Quote.
 *
 * @author David Koschnick
 */

public class Rank extends DibsObject {
    private String quotaId;
    private String userId;
    private String applicationId;
    private int index;
    private int lotnumber;

    Rank(Map<String, Object> args) {
        super(args);
        quotaId = (String) args.get("quota_id");
        userId = (String) args.get("user_id");
        applicationId = (String) args.get("application_id");
        index = (Integer) args.get("index");
        lotnumber = (Integer) args.get("lotnumber");
    }

    /**
     * Legt einen neuen Ranglisteneintrag an.
     */
    public static Rank create(Map<String, Object> args) {
        try {
            ApplicationService service = (ApplicationService) args.get("service");
            String id = "rank:" + Integer.toString(new Random().nextInt());
            service.getQueryRunner().insert(service.getDb(),
                "INSERT INTO rank VALUES(?, ?, ?, ?, ?, ?)", new MapHandler(), id,
                args.get("quota_id"), args.get("user_id"), args.get("application_id"),
                args.get("index"), args.get("lotnumber"));
            args.put("id", id);
            return new Rank(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Quote zurück.
     */
    public Quota getQuota() {
        return service.getQuota(quotaId);
    }

    /**
     * Gibt den User zurück.
     */
    public User getUser() {
        return service.getUser(this.userId);
    }

    /**
     * Gibt die Bewerbung zurück.
     */
    public Application getApplication() {
        return service.getApplication(applicationId);
    }

    /**
     * Gibt den Ranglistenplatz zurück.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gibt die Losnummer zurück.
     */
    public int getLotnumber() {
        return lotnumber;
    }
}
