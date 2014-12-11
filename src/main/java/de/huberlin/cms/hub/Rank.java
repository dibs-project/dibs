package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.handlers.MapHandler;


/**
 * Rangliste für eine Quote.
 *
 * @author David Koschnick
 */

public class Rank extends HubObject {
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
    public static Rank create(Map<String, Object> args, boolean doCommit) {
        try {
            ApplicationService service = (ApplicationService) args.get("service");
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String id = "rank:" + Integer.toString(new Random().nextInt());
            service.getQueryRunner().insert(service.getDb(),
                "INSERT INTO rank VALUES(?, ?, ?, ?, ?, ?)", new MapHandler(), id,
                args.get("quota_id"), args.get("user_id"), args.get("application_id"),
                args.get("index"), args.get("lotnumber"));
            if (doCommit) {
                db.commit();
                db.setAutoCommit(true);
            }
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
