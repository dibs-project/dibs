/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 *
 * @author Sven Pfaller
 * @author Markus Michler
 */
public class User extends HubObject {
    private String name;
    private String email;

    User(Map<String, Object> args) {
        super(args);
        this.name = (String) args.get("name");
        this.email = (String) args.get("email");
    }

    /**
     * Legt eine neue Information für diesen Benutzer an.
     *
     * @param typeId ID des Informationstyps
     * @param args Argumente zum Erstellen der Information. Für eine genaue Beschreibung
     *     siehe die Dokumentation von <code>create</code> des jeweiligen
     *     Informationstyps.
     * @param agent ausführender Benutzer
     * @return angelegte Information
     */
    public Information createInformation(String typeId, HashMap<String, Object> args,
            User agent) {
        Information.Type type = this.service.getInformationTypes().get(typeId);
        if (type == null) {
            throw new IllegalArgumentException("illegal typeId: unknown");
        }
        Information information = type.create(args, this, agent);

        // Pseudo-Ereignis auslösen
        for (Application application : this.getApplications(null)) {
            application.userInformationCreated(this, information);
        }

        return information;
    }

    /**
     * Gibt eine Liste aller Informationen dieses Benutzers zurück.
     *
     * @param agent ausführender Benutzer
     * @return Liste aller Informationen dieses Benutzers
     */
    public List<Information> getInformationSet(User agent) {
        ArrayList<Information> informationSet = new ArrayList<Information>();
        for (Information.Type type : service.getInformationTypes().values()) {
            try {
                String sql = String.format("SELECT * FROM \"%s\" WHERE user_id = ?", type.getId());
                List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
                queryResults = service.getQueryRunner().query(service.getDb(),
                    sql, service.getMapListHandler(), this.id);
                for (Map<String, Object> args : queryResults) {
                    informationSet.add(type.newInstance(args, service));
                }
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
        return informationSet;
    }

    /**
     * Gibt alle Bewerbungen des Benutzers zurück.
     *
     * @param agent ausführender Benutzer
     * @return Liste aller Bewerbungen des Benutzers
     */
    public List<Application> getApplications(User agent) {
        try {
            List<Application> applications = new ArrayList<Application>();
            List<Map<String, Object>> queryResults = service.getQueryRunner().query(
                service.getDb(), "SELECT * FROM application WHERE user_id = ?",
                service.getMapListHandler(), this.getId());
            for(Map<String, Object> args : queryResults) {
               args.put("service", service);
               applications.add(new Application(args));
           }
            return applications;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Name, mit dem der Benutzer von HUB angesprochen wird.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Email-Adresse.
     */
    public String getEmail() {
        return this.email;
    }
}
