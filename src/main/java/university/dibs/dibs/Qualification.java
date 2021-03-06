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

import static university.dibs.dibs.Util.isInRange;

import org.apache.commons.dbutils.handlers.MapHandler;

import java.io.IOError;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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

    /* ---- Properties ---- */

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

    /* ---- /Properties ---- */

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
         * @return TODO
         */
        @Override
        public Information create(Map<String, Object> args, User user, User agent) {
            Set<String> keys = new HashSet<String>(Arrays.asList("grade"));
            if (!args.keySet().equals(keys)) {
                throw new IllegalArgumentException("illegal args: improper keys");
            }
            double grade = (Double) args.get("grade");
            if (!isInRange(grade, 1.0, 6.0)) {
                throw new IllegalArgumentException("args_grade_out_of_range");
            }

            ApplicationService service = user.getService();

            try {
                service.beginTransaction();
                String id = "qualification:" + Integer.toString(new Random().nextInt());
                service.getQueryRunner().insert(service.getDb(),
                    "INSERT INTO qualification VALUES (?, ?, ?)", new MapHandler(), id,
                    user.getId(), grade);
                service.getJournal().record(ApplicationService.ACTION_TYPE_INFORMATION_CREATED,
                    user.getId(), DibsObject.getId(agent), id);
                service.endTransaction();
                return service.getInformation(id);
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }
}
