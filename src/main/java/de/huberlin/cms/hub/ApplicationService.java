/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.collections4.CollectionUtils.filter;

import java.io.IOError;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import org.apache.commons.collections4.Predicate;

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
    /** Unterstützte Filter für {@link #getCriteria(Map, User)}. */
    public static final Set<String> GET_CRITERIA_FILTER_KEYS =
        new HashSet<String>(Arrays.asList("required_information_type_id"));

    private Properties config;
    private Connection db;
    private Journal journal;
    private HashMap<String, Information.Type> informationTypes;
    private HashMap<String, Criterion> criteria;

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

        this.criteria = new HashMap<String, Criterion>();
        this.criteria.put("qualification", new QualificationCriterion("qualification",
            informationTypes.get("qualification"), this));
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
    * Registriert einen neuen Benutzer.
    *
    * @param name Name, mit dem der Benutzer von HUB angesprochen wird
    * @param email Email-Adresse
    * @return Registrierter Nutzer
    */
    public User register(String name, String email) {
        return this.createUser(name, email);
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
     * Gibt eine Bewerbung über die ID zurück.
     *
     * @param id ID der Bewerbung
     * @return Bewerbung mit der spezifizierten ID
     */
    public Application getApplication(String id) {
        try {
            String sql = "SELECT * FROM application WHERE id = ?";
            PreparedStatement statement = this.db.prepareStatement(sql);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException(
                    "illegal id: application does not exist");
            }
            HashMap<String, Object> args = new HashMap<String, Object>();
            args.put("id", results.getString("id"));
            args.put("service", this);
            args.put("user_id", results.getString("user_id"));
            args.put("course_id", results.getString("course_id"));
            args.put("status", results.getString("status"));
            return new Application(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Bewertung mit der spezifizierten ID zurück.
     *
     * @param id ID der Bewertung
     * @param agent ausführender Benutzer
     * @return Bewertung mit der spezifizierten ID
     */
    public Evaluation getEvaluation(String id, User agent) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM evaluation WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException(
                    "illegal id: evaluation does not exist");
            }
            HashMap<String, Object> args = new HashMap<String, Object>();
            args.put("id", results.getString("id"));
            args.put("application_id", results.getString("application_id"));
            args.put("criterion_id", results.getString("criterion_id"));
            args.put("information_id", results.getString("information_id"));
            args.put("value", results.getObject("value"));
            args.put("status", results.getString("status"));
            args.put("service", this);
            return new Evaluation(args);
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
     * @param agent ausführender Benutzer
     * @return angelegter Studiengang
     * @throws IllegalArgumentException wenn <code>name</code> leer ist oder
     *     <code>capacity</code> nicht positiv ist
     */
    public Course createCourse(String name, int capacity, User agent) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("illegal capacity: nonpositive number");
        }

        try {
            this.db.setAutoCommit(false);
            String id = "course:" + Integer.toString(new Random().nextInt());
            PreparedStatement statement =
                db.prepareStatement("INSERT INTO course VALUES(?, ?, ?)");
            statement.setString(1, id);
            statement.setString(2, name);
            statement.setInt(3, capacity);
            statement.executeUpdate();
            journal.record(ActionType.COURSE_CREATED, null, null, HubObject.getId(agent),
                name);
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
     * Gibt das Vergabeschema mit der spezifizierten ID zurück.
     *
     * @param id ID des Vergabeschemas
     * @return Vergabeschema mit der spezifizierten ID
     */
    public AllocationRule getAllocationRule(String id) {
        try {
            PreparedStatement statement =
                this.db.prepareStatement("SELECT * FROM allocation_rule WHERE id = ?");
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException(
                    "illegal id: allocation rule does not exist");
            }
            return new AllocationRule(results, this);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt die Quote mit der spezifizierten ID zurück.
     *
     * @param id ID der Quote
     * @return Quote mit der spezifizierten ID
     */
    public Quota getQuota(String id) {
        try {
            String sql = "SELECT * FROM quota WHERE id = ?";
            PreparedStatement statement = this.db.prepareStatement(sql);
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new IllegalArgumentException("illegal id: quota does not exist");
            }
            HashMap<String, Object> args = new HashMap<String, Object>();
            args.put("id", id);
            args.put("name", results.getString("name"));
            args.put("percentage", results.getInt("percentage"));
            args.put("service", this);
            return new Quota(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Gibt eine Liste aller Kriterien zurück.
     *
     * @param filter Filter
     * @param agent ausführender Benutzer
     * @return Liste aller Kriterien
     */
    public List<Criterion> getCriteria(Map<String, Object> filter, User agent) {
        if (!GET_CRITERIA_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("illegal filter: improper keys");
        }

        ArrayList<Criterion> criteria = new ArrayList<Criterion>(this.criteria.values());
        final String requiredInformationTypeId =
            (String) filter.get("required_information_type_id");
        if (requiredInformationTypeId != null) {
            filter(criteria,
                new Predicate<Criterion>() {
                    public boolean evaluate(Criterion object) {
                        return object.getRequiredInformationType().getId().equals(
                            requiredInformationTypeId);
                    }
                });
        }
        return criteria;
    }

    /**
     * Gibt eine Liste aller Kriterien zurück.
     *
     * @see #getCriteria(Map, User)
     */
    public List<Criterion> getCriteria(User agent) {
        return this.getCriteria(new HashMap<String, Object>(), agent);
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

    /**
     * Verfügbare Kriterien (indiziert nach ID).
     */
    public Map<String, Criterion> getCriteria() { 
        return unmodifiableMap(this.criteria); 
    }
}
