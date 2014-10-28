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
import java.util.Map;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import de.huberlin.cms.hub.HubException.ObjectNotFoundException;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 *
 * @author Sven Pfaller
 * @author Markus Michler
 */
public class User extends HubObject {
    private String name;
    private String email;
    private QueryRunner queryRunner;

    User(Map<String, Object> args) {
        super((String)args.get("id"), (ApplicationService)args.get("service"));
        this.name = (String)args.get("name");
        this.email = (String)args.get("email");
        this.queryRunner = new QueryRunner();
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
//            try {
//                Map<String, Object> args = this.queryRunner.query(this.service.getDb(),
//                    "SELECT * FROM \"%s\" WHERE user_id = ?",
//                    new MapHandler(), type.getId(), id);
//                if (args == null) {
//                    throw new ObjectNotFoundException(id);
//                }
//                informationSet.add(type.newInstance(args, this.service));
//            } catch (SQLException e) {
//                throw new IOError(e);
//            }
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
            List<Map<String, Object>> queryResults = this.queryRunner.query(
                this.service.getDb(), "SELECT * FROM application WHERE user_id = ?",
                new MapListHandler(), this.getId());
            for(Map<String, Object> args : queryResults) {
               args.put("service", this.getService());
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
