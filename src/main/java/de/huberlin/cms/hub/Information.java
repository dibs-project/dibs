/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Information eines Benutzers.
 *
 * @author Sven Pfaller
 */
public abstract class Information extends HubObject {
    protected String userId;

    protected Information(String id, String userId, ApplicationService service) {
        super(id, service);
        this.userId = userId;
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
         * initialisiert sie über einen Datenbankcursor.
         *
         * @param args Datenbankcursor, der auf ein passendes Abfrageergebnis verweist
         * @param service Bewerbungsdienst
         * @return neue Information
         * @throws SQLException wenn ein Datenbankzugriffsfehler auftritt
         */
//        public abstract Information newInstance(Map<String, Object> args,
//            ApplicationService service) throws SQLException;
        
        public abstract Information newInstance(ResultSet results,
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
