package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;


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

    Rank(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        quotaId = (String) args.get("quota_id");
        userId = (String) args.get("user_id");
        applicationId = (String) args.get("application_id");
        index = (Integer) args.get("index");
        lotnumber = (Integer) args.get("lotnumber");
    }

    /**
     * Legt einen neuen Ranglisteneintrag an.
     */
    public static Rank create(HashMap<String, Object> args) {
        try {
            ApplicationService service = (ApplicationService) args.get("service");
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String id = "rank:" + Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO rank VALUES(?, ?, ?, ?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, (String) args.get("quota_id"));
            statement.setString(3, (String) args.get("user_id"));
            statement.setString(4, (String) args.get("application_id"));
            statement.setInt(5, (int) args.get("index"));
            statement.setInt(6, (int) args.get("lotnumber"));
            statement.executeUpdate();
            db.commit();
            db.setAutoCommit(true);
            args.put("id", id);
            return new Rank(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Id der Quote zurück.
     */
    public String getQuotaId() {
        return quotaId;
    }

    /**
     * Gibt die Id des Users zurück.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Gibt die Id der Bewerbung zurück.
     */
    public String getApplicationId() {
        return applicationId;
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
