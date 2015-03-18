/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import static university.dibs.dibs.Util.isInRange;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.apache.commons.dbutils.handlers.MapHandler;

/**
 * Hochschulreife (bzw. Hochschulzugangsberechtigung).
 *
 * @author Sven Pfaller
 */
public class Qualification extends Information {
    private double grade;

    Qualification(Map<String, Object> args) {
        super(args);
        this.grade = (Double) args.get("grade");
    }

    /**
     * Note im Bereich von 1,0 bis 6,0.
     */
    public double getGrade() {
        return this.grade;
    }

    @Override
    public Information.Type getType() {
        return this.service.getInformationTypes().get("qualification");
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
        public Information newInstance(Map<String, Object> args, ApplicationService service)
                throws SQLException {
            args.put("service", service);
            return new Qualification(args);
        }

        /**
         * Legt eine neue Hochschulreife für einen Benutzer an.
         *
         * @param args Argumente zum Erstellen der Hochschulreife. <code>grade</code> ist
         *     die Note im Bereich 1,0 bis 6,0.
         */
        @Override
        public Information create(Map<String, Object> args, User user, User agent) {
            HashSet<String> keys = new HashSet<String>(Arrays.asList("grade"));
            if (!args.keySet().equals(keys)) {
                throw new IllegalArgumentException("illegal args: improper keys");
            }
            double grade = (Double) args.get("grade");
            if (!isInRange(grade, 1.0, 6.0)) {
                throw new IllegalArgumentException("args_grade_out_of_range");
            }

            ApplicationService service = user.getService();
            Connection db = service.getDb();

            try {
                db.setAutoCommit(false);
                String id = "qualification:" + Integer.toString(new Random().nextInt());
                service.getQueryRunner().insert(service.getDb(), "INSERT INTO qualification VALUES (?, ?, ?)",
                    new MapHandler(), id, user.getId(), grade);
                service.getJournal().record(ApplicationService.ACTION_TYPE_INFORMATION_CREATED,
                    user.getId(), DibsObject.getId(agent), id);
                db.commit();
                db.setAutoCommit(true);
                return service.getInformation(id);
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }
}
