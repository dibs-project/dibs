/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static java.util.Collections.unmodifiableMap;

import java.io.IOError;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import de.huberlin.cms.hub.JournalRecord.ActionType;

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
    private Journal journal;
    private HashMap<String, Information.Type> informationTypes;

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
        this.journal = new Journal(this);

        this.informationTypes = new HashMap<String, Information.Type>();
        this.informationTypes.put("qualification", new Qualification.Type());
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
            this.db.setAutoCommit(false);
            // TODO: besseres Format für zufällige IDs
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO \"user\" VALUES(?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setString(3, email);
            statement.executeUpdate();
            journal.record(ActionType.USER_CREATED, null, null, null, id);
            this.db.commit();
            this.db.setAutoCommit(true);
            return this.getUser(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt den Benutzer mit der spezifizierten ID zurück.
     *
     * @param id ID des Benutzers
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
            return new User(results, this);
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
                users.add(new User(results, this));
            }
            return users;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Information mit der spezifizierten ID zurück.
     *
     * @param id ID der Information
     * @return Information mit der spezifizierten ID
     */
    public Information getInformation(String id) {
        String typeId = id.split(":")[0];
        Information.Type type = this.informationTypes.get(typeId);
        if (type == null) {
            throw new IllegalArgumentException("illegal id: information does not exist");
        }

        try {
            PreparedStatement statement = this.db.prepareStatement(
                String.format("SELECT * FROM \"%s\" WHERE id = ?", typeId));
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException(
                    "illegal id: information does not exist");
            }
            return type.newInstance(results, this);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Stellt das aktuelle Semester für das Bewerbungssystem ein.
     *
     * @param semester Neues aktuelles Semester.
     * @see Settings#getSemester()
     */
    public void setSemester(String semester) {
        try {
            PreparedStatement statement =
                db.prepareStatement("UPDATE settings SET semester = ?");
            statement.setString(1, semester);
            statement.executeUpdate();
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
            return new Settings(results, this);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Legt einen neuen Studiengang an.
     *
     * @param name Name des Studiengangs
     * @param capacity Kapazität des Studiengangs
     * @param user Benutzer, der den Studiengang anlegt
     * @return angelegter Studiengang
     * @throws IllegalArgumentException wenn <code>name</code> leer ist oder
     *     <code>capacity</code> nicht positiv ist
     */
    public Course createCourse(String name, int capacity, User user) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("illegal capacity: nonpositive number");
        }

        try {
            this.db.setAutoCommit(false);
            String id = Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO course VALUES(?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setInt(3, capacity);
            statement.executeUpdate();
            journal.record(ActionType.COURSE_CREATED, null, null, user.getId(), name);
            this.db.commit();
            this.db.setAutoCommit(true);
            return this.getCourse(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

     /**
     * Gibt den Studiengang mit der spezifizierten ID zurück.
     *
     * @param id ID des Studiengangs
     * @return Studiengang mit der spezifizierten ID
     * @throws IllegalArgumentException wenn kein Studiengang mit der spezifizierten ID
     *     existiert
     */
    public Course getCourse(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM course WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: course does not exist");
            }
            return new Course(results, this);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt eine Liste aller Studiengänge zurück.
     *
     * @return Liste aller Studiengänge
     */
    public List<Course> getCourses() {
        try {
            ArrayList<Course> courses = new ArrayList<Course>();
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM course");
            ResultSet results = statement.executeQuery();
            while (results.next()) {
                courses.add(new Course(results, this));
            }
            return courses;
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Das Protokollbuch des Bewerbungsdienstes.
     */
    public Journal getJournal() {
        return new Journal(this);
    }

    /**
     * Verfügbare Informationstypen (indiziert nach ID).
     */
    public Map<String, Information.Type> getInformationTypes() {
        return unmodifiableMap(this.informationTypes);
    }
}
