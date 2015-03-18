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

import java.sql.SQLException;
import java.util.Map;

/**
 * Information eines Benutzers.
 *
 * @author Sven Pfaller
 */
public abstract class Information extends DibsObject {
    protected String userId;

    protected Information(Map<String, Object> args) {
        super(args);
        this.userId = (String) args.get("user_id");
    }

    /**
     * Zugehöriger Benutzer.
     */
    public User getUser() {
        return this.service.getUser(this.userId);
    }

    /**
     * Typ dieser Information.
     */
    public abstract Information.Type getType();

    /**
     * Beschreibung eines Informationstyps.
     *
     * @author Sven Pfaller
     */
    public static abstract class Type {
        protected String id;

        protected Type(String id) {
            this.id = id;
        }

        /**
         * Extension API: Erstellt eine neue Instanz dieses Informationstyps und
         * initialisiert sie über die Argumente.
         *
         * @param args Argumente zum Initialisieren der Information.
         * @param service Bewerbungsdienst
         * @return neue Information
         * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt
         */
        public abstract Information newInstance(Map<String, Object> args,
            ApplicationService service) throws SQLException;

        /**
         * Legt eine neue Information für einen Benutzer an.
         *
         * @param args Argumente zum Erstellen der Information. Für eine genaue
         *     Beschreibung siehe die Dokumentation der jeweiligen Unterklasse.
         * @param user Benutzer, für den die Information angelegt wird
         * @param agent ausführender Benutzer
         * @return angelegte Information
         */
        public abstract Information create(Map<String, Object> args, User user,
            User agent);

        /**
         * Eindeutige ID.
         */
        public String getId() {
            return this.id;
        }
    }
}
