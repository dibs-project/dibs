/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 *
 * @author Sven Pfaller
 * @author Markus Michler
 */
public class User extends HubObject {
    private String name;
    private String email;

    User(String id, String name, String email, ApplicationService service) {
        super(id, service);
        this.name = name;
        this.email = email;
    }

    User(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Benutzer über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getString("email"), service);
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
        for (Information.Type type : this.service.getInformationTypes().values()) {
            try {
                PreparedStatement statement = this.service.getDb().prepareStatement(
                    String.format("SELECT * FROM \"%s\" WHERE user_id = ?",
                        type.getId()));
                statement.setString(1, this.id);
                ResultSet results = statement.executeQuery();
                while (results.next()) {
                    informationSet.add(type.newInstance(results, this.service));
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
            String sql = "SELECT * FROM application WHERE user_id = ?";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                HashMap<String, Object> args = new HashMap<String, Object>();
                args.put("id", results.getString("id"));
                args.put("service", this.getService());
                args.put("user_id", results.getString("user_id"));
                args.put("course_id", results.getString("course_id"));
                args.put("status", results.getString("status"));
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
