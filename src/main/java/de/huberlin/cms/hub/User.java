/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
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
    private String dosvBid;
    private String dosvBan;

    User(String id, String name, String email, String dosvBid, String dosvBan,
        ApplicationService service) {
        super(id, service);
        this.name = name;
        this.email = email;
        this.dosvBid = dosvBid;
        this.dosvBan = dosvBan;
    }

    User(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert den Benutzer über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getString("email"), results.getString("dosv_bid"),
            results.getString("dosv_ban"), service);
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
     * Verbindet den Benutzer mit dem System des DoSV. Speichert bei Erfolg BID und BAN
     * bei den Benutzerdaten.
     *
     * @param dosvBid DoSV-Benutzer-ID
     * @param dosvBan DOSV-Benutzer-Autorisierungsnummer
     *
     * @return <code>true</code>, wenn der Benutzer verbunden wurde,
     * <code>false</code> wenn er nicht authentifiziert werden konnte.
     *
     * @see de.huberlin.cms.hub.dosv.DosvSync#authenticate(String, String)
     */
    public boolean connectToDosv(String dosvBid, String dosvBan, User agent) {
        if (!service.getDosvSync().authenticate(dosvBid, dosvBan)) {
            return false;
        };
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            String sql = "UPDATE \"user\" SET dosv_bid = ?, dosv_ban = ? WHERE id = ?";
            PreparedStatement statement = db.prepareStatement(sql);
            statement.setString(1, dosvBid);
            statement.setString(2, dosvBan);
            statement.setString(3, id);
            statement.executeUpdate();
            this.dosvBid = dosvBid;
            this.dosvBan = dosvBan;
            service.getJournal().record(ApplicationService.APPLICATION_TYPE_USER_CONNECTED_TO_DOSV,
                id, HubObject.getId(agent), null);
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            // TODO Fehler bei Verletzung unique-constraint dosv_bid abfangen
            throw new IOError(e);
        }
        return true;
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

    /**
     * Bewerber-ID für das DoSV.
     */
    public String getDosvBid() {
        return dosvBid;
    }

    /**
     *  Bewerber-Autorisierungsnummer für das DoSV.
     */
    public String getDosvBan() {
        return dosvBan;
    }
}
