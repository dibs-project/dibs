/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static de.huberlin.cms.hub.Util.isInRange;

import java.io.IOError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;
import de.huberlin.cms.hub.JournalRecord.ObjectType;

/**
 * Hochschulreife (bzw. Hochschulzugangsberechtigung).
 *
 * @author Sven Pfaller
 */
public class Qualification extends Information {
    private double grade;

    Qualification(String id, String userId, double grade, ApplicationService service) {
        super(id, userId, service);
        this.grade = grade;
    }

    Qualification(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Hochschulreife über den Datenbankcursor
        this(results.getString("id"), results.getString("user_id"),
            results.getDouble("grade"), service);
    }

    /**
     * Note im Bereich von 1,0 bis 6,0.
     */
    public double getGrade() {
        return this.grade;
    }

    /**
     * Beschreibung des Informationstyps Hochschulreife.
     *
     * @author Sven Pfaller
     */
    public static class Type extends Information.Type {
        /**
         * Initialisiert den Informationstyp.
         */
        public Type() {
            super("qualification");
        }

        @Override
        public Information newInstance(ResultSet results, ApplicationService service)
                throws SQLException {
            return new Qualification(results, service);
        }

        /**
         * Legt eine neue Hochschulreife für einen Benutzer an.
         *
         * @param args Argumente zum Erstellen der Hochschulreife. <code>grade</code> ist
         *     die Note im Bereich 1,0 bis 6,0.
         */
        @Override
        public Information create(HashMap<String, Object> args, User user, User agent) {
            HashSet<String> keys = new HashSet<String>(Arrays.asList("grade"));
            if (!args.keySet().equals(keys)) {
                throw new IllegalArgumentException("illegal args: improper keys");
            }
            double grade = (Double) args.get("grade");
            if (!isInRange(grade, 1.0, 6.0)) {
                throw new IllegalArgumentException("illegal grade in args: out of range");
            }

            ApplicationService service = user.getService();
            Connection db = service.getDb();

            try {
                db.setAutoCommit(false);
                String id = "qualification:" + Integer.toString(new Random().nextInt());
                PreparedStatement statement =
                    db.prepareStatement("INSERT INTO qualification VALUES (?, ?, ?)");
                statement.setString(1, id);
                statement.setString(2, user.getId());
                statement.setDouble(3, grade);
                statement.executeUpdate();
                service.getJournal().record(ActionType.INFORMATION_CREATED,
                    ObjectType.USER, user.getId(), HubObject.getId(agent), id);
                db.commit();
                db.setAutoCommit(true);
                return service.getInformation(id);
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }
}
