/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

/**
 * Repräsentiert den Bewerbungsdienst, bzw. den Bewerbungsprozess.
 * <p>
 * ApplicationService an sich ist nicht threadsafe, aber ein leichtgewichtiges Objekt.
 * Soll er in mehreren Threads benutzt werden (z.B. in einer Webanwendung) kann einfach
 * für jeden Thread ein eigenes Objekt erstellt werden.
 *
 * @author Sven Pfaller
 */
public class ApplicationService {
    private Properties config;
    private Connection db;

    /**
     * Stellt eine Verbindung zur Datenbank her.
     *
     * @param config Konfiguration. Folgende Einstellungen können gesetzt werden:
     *     <ul>
     *         <li>
     *             db_url: Datenbank-URL in JDBC-Form (siehe
     *             {@link DriverManager#getConnection}). Der Standardwert ist
     *             "jdbc:postgresql://localhost:5432/hub".
     *         </li>
     *         <li>db_user: Benutzername für die Datenbank. Der Standardwert ist "".</li>
     *         <li>db_password: Passwort für die Datenbank. Der Standardwert ist "".</li>
     *     </ul>
     * @return geöffnete Datenbankverbindung
     * @throws SQLException falls die Verbindung zur Datenbank fehlschlägt
     * @see DriverManager#getConnection
     */
    public static Connection openDatabase(Properties config) throws SQLException {
        String url = config.getProperty("db_url", "jdbc:postgresql://localhost:5432/hub");
        String user = config.getProperty("db_user", "");
        String password = config.getProperty("db_password", "");
        Connection db = DriverManager.getConnection(url, user, password);
        return db;
    }

    /**
     * Initialisiert den ApplicationService.
     *
     * @param db Datenbankverbindung, die verwendet werden soll.
     * @param config Konfiguration.
     * @see #getConfig()
     */
    public ApplicationService(Connection db, Properties config) {
        this.db = db;

        Properties defaults = new Properties();
        defaults.setProperty("dosv_url",
            "https://hsst.hochschulstart.de/hochschule/webservice/2/");
        defaults.setProperty("dosv_user", "");
        defaults.setProperty("dosv_password", "");
        this.config = new Properties(defaults);
        this.config.putAll(config);
    }

    /**
     * Legt einen neuen Benutzer an.
     *
     * @param name Name, mit dem der Benutzer von HUB angesprochen wird
     * @param email Email-Adresse
     * @return Angelegter Benutzer
     * @throws IllegalArgumentException wenn <code>name</code> oder <code>email</code>
     *     leer ist
     */
    public User createUser(String name, String email) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("illegal email: empty");
        }

        try {
            // TODO: besseres Format für zufällige IDs
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO \"user\" VALUES(?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.executeUpdate();
            return this.getUser(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt den Benutzer mit der spezifizierten ID zurück.
     *
     * @param id ID des Benutzer
     * @return Benutzer mit der spezifizierten ID
     * @throws IllegalArgumentException wenn kein Benutzer mit der spezifizierten ID
     *     existiert
     */
    public User getUser(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM \"user\" WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: user does not exist");
            }
            return new User(results);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt eine Liste aller Benutzer zurück.
     *
     * @return Liste aller Benutzer
     */
    public List<User> getUsers() {
        try {
            ArrayList<User> users = new ArrayList<User>();
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM \"user\"");
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                users.add(new User(results));
            }
            return users;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Stellt das aktuelle Semester für das Bewerbungssystem ein.
     *
     * @param semester Neues aktuelle Semester.
     * @see Settings#getSemester()
     */
    public void setSemester(String semester) {
        try {
            PreparedStatement statement =
                db.prepareStatement("UPDATE settings SET semester = ?");
            statement.setString(1, semester);
            statement.executeUpdate();
            db.commit();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Verwendete Datenbankverbindung.
     */
    public Connection getDb() {
        return db;
    }

    /**
     * Konfiguration. Folgende Einstellungen können gesetzt werden:
     * <ul>
     *     <li>
     *         dosv_url: URL zur DoSV-API. Der Standardwert ist
     *         "https://hsst.hochschulstart.de/hochschule/webservice/2/" (Testumgebung).
     *     </li>
     *     <li>dosv_user: Benutzername für die DoSV-API. Der Standardwert ist "".</li>
     *     <li>dosv_password: Passwort für die DoSV-API. Der Standardwert ist "".</li>
     * </ul>
     */
    public Properties getConfig() {
        return config;
    }

    /**
     * Gibt die Einstellungen des Bewerbungssystems zurück.
     *
     * @return Einstellungen des Bewerbungssystems
     */
    public Settings getSettings() {
        try {
            PreparedStatement statement = db.prepareStatement("SELECT * FROM settings");
            ResultSet results = statement.executeQuery();
            results.next();
            return new Settings(results);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt den Applicant mit der spezifizierten ID zurück.
     *
     * @param id ID des Applicants
     * @return Applicant mit der spezifizierten ID
     * @throws IllegalArgumentException falls die ID ungültig ist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    public Applicant getApplicant(int id) throws SQLException {
        PreparedStatement statement =
            db.prepareStatement("SELECT * FROM onlbew.onlbew_reg WHERE reg_id=?");
        statement.setInt(1, id);
        ResultSet results = statement.executeQuery();
        if (!results.next()) {
            throw new IllegalArgumentException("invalid applicant ID");
        }
        return new Applicant(results);
    }

    /**
     * Gibt das Logbuch.
     *
     * @return Journal
     */
    public Journal getJournal() {
        return new Journal(this);
    }
}
