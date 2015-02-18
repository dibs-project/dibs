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

import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.io.IOError;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 *
 * @author Sven Pfaller
 * @author Markus Michler
 */
public class User extends DibsObject {
    /** Role: applicant. */
    public static final String ROLE_APPLICANT = "applicant";
    /** Role: administrator. */
    public static final String ROLE_ADMIN = "admin";

    private String name;
    private String email;
    private String credential;
    private String role;
    private String dosvBid;
    private String dosvBan;

    /**
     * Initializes User.
     *
     * @param args TODO
     */
    User(Map<String, Object> args) {
        super(args);
        this.name = (String) args.get("name");
        this.email = (String) args.get("email");
        this.credential = (String) args.get("credential");
        this.role = (String) args.get("role");
        this.dosvBid = (String) args.get("dosv_bid");
        this.dosvBan = (String) args.get("dosv_ban");
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
    public Information createInformation(String typeId, Map<String, Object> args,
            User agent) {
        Information.Type type = this.service.getInformationTypes().get(typeId);
        if (type == null) {
            throw new IllegalArgumentException("illegal typeId: unknown");
        }
        Information information = type.create(args, this, agent);

        // Pseudo-Ereignis auslösen
        for (Application application : this.getApplications()) {
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
        List<Information> informationSet = new ArrayList<Information>();
        for (Information.Type type : service.getInformationTypes().values()) {
            try {
                String sql = String.format("SELECT * FROM \"%s\" WHERE user_id = ?", type.getId());
                List<Map<String, Object>> queryResults = new ArrayList<Map<String, Object>>();
                queryResults = service.getQueryRunner().query(service.getDb(),
                    sql, new MapListHandler(), this.id);
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
     * Returns the User's Information for a given Type.
     * @param typeId ID of the Information.Type
     * @return Information with the requested Type
     */
    public Information getInformationByType(String typeId) {
        Information.Type type = service.getInformationTypes().get(typeId);
        if (type == null) {
            throw new IllegalArgumentException("illegal typeId: unknown");
        }
        try {
            String sql = String.format("SELECT * FROM \"%s\" WHERE user_id = ?", typeId);
            Map<String, Object> args = service.getQueryRunner().query(service.getDb(),
                sql, new MapHandler(), id);
            if (args == null) {
                throw new DibsException.ObjectNotFoundException(typeId);
            }
            return type.newInstance(args, service);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Returns a list of the user's applications.
     *
     * @return list of the user's applications
     */
    public List<Application> getApplications() {
        try {
            List<Application> applications = new ArrayList<Application>();
            List<Map<String, Object>> queryResults = service.getQueryRunner().query(
                service.getDb(), "SELECT * FROM application WHERE user_id = ?",
                new MapListHandler(), this.getId());
            for (Map<String, Object> args : queryResults) {
                args.put("service", service);
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
     * @param agent TODO
     *
     * @return <code>true</code>, wenn der Benutzer verbunden wurde,
     * <code>false</code> wenn er nicht authentifiziert werden konnte.
     *
     * @see university.dibs.dibs.dosv.DosvSync#authenticate(String, String)
     */
    public boolean connectToDosv(String dosvBid, String dosvBan, User agent) {
        if (!service.getDosvSync().authenticate(dosvBid, dosvBan)) {
            return false;
        }
        try {
            this.service.beginTransaction();
            service.getQueryRunner().update(this.service.getDb(),
                "UPDATE \"user\" SET dosv_bid = ?, dosv_ban = ? WHERE id = ?", dosvBid,
                dosvBan, id);
            this.dosvBid = dosvBid;
            this.dosvBan = dosvBan;
            service.getJournal().record(ApplicationService.APPLICATION_TYPE_USER_CONNECTED_TO_DOSV,
                id, DibsObject.getId(agent), null);
            this.service.endTransaction();
        } catch (SQLException e) {
            // TODO Fehler bei Verletzung unique-constraint dosv_bid abfangen
            throw new IOError(e);
        }
        return true;
    }

    /* ---- Properties ---- */

    /**
     * Name which dibs uses to address the user.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Email address.
     */
    public String getEmail() {
        return this.email;
    }

    /**
     * Credential.
     */
    public String getCredential() {
        return this.credential;
    }

    /**
     * Role.
     */
    public String getRole() {
        return this.role;
    }

    /**
     * Bewerber-ID für das DoSV.
     */
    public String getDosvBid() {
        return this.dosvBid;
    }

    /**
     *  Bewerber-Autorisierungsnummer für das DoSV.
     */
    public String getDosvBan() {
        return this.dosvBan;
    }

    /* ---- /Properties ---- */
}
