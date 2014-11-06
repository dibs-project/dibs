/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.collections4.CollectionUtils.filter;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
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

import de.huberlin.cms.hub.HubException.ObjectNotFoundException;
import de.huberlin.cms.hub.HubException.IllegalStateException;

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
    /** Benötigte Version des Datenspeichers. */
    public static final String STORAGE_VERSION = "0";

    /** Aktionstyp: neuer Benutzer angelegt. */
    public static final String ACTION_TYPE_USER_CREATED = "user_created";
    /** Aktionstyp: neue Information für einen Benutzer angelegt. */
    public static final String ACTION_TYPE_INFORMATION_CREATED = "information_created";
    /** Aktionstyp: neuer Studiengang angelegt. */
    public static final String ACTION_TYPE_COURSE_CREATED = "course_created";
    /** Aktionstyp: neue Vergaberegel angelegt und mit dem Studiengang verknüpft. */
    public static final String ACTION_TYPE_COURSE_ALLOCATION_RULE_CREATED =
        "course_allocation_rule_created";
    /** Aktionstyp: Kurs publiziert (zur Bewerbung freigegeben). */
    public static final String ACTION_TYPE_COURSE_PUBLISHED = "course_published";
    /** Aktionstyp: Kurspublikation zurückgezogen. */
    public static final String ACTION_TYPE_COURSE_UNPUBLISHED = "course_unpublished";
    /** Aktionstyp: Bewerbung für den Studiengang angelegt. */
    public static final String ACTION_TYPE_COURSE_APPLIED = "course_applied";
    /** Aktionstyp: Bewerbungstatus bearbeitet. */
    public static final String ACTION_TYPE_APPLICATION_STATUS_SET = "application_status_set";
    /** Aktionstyp: Quote erstellt und mit der Vergaberegel verknüpft. */
    public static final String ACTION_TYPE_ALLOCATION_RULE_QUOTA_CREATED =
        "allocation_rule_quota_created";
    /** Aktionstyp: Kriterium zur Sortierung von Bewerbern mit Quote verknüpft. */
    public static final String ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED =
        "quota_ranking_criterion_added";

    /** Unterstützte Filter für {@link #getCriteria(Map, User)}. */
    public static final Set<String> GET_CRITERIA_FILTER_KEYS =
        new HashSet<String>(Arrays.asList("required_information_type_id"));

    private Properties config;
    private Connection db;
    private Journal journal;
    private HashMap<String, Information.Type> informationTypes;
    private HashMap<String, Criterion> criteria;

    // TODO: dokumentieren
    public static void setupStorage(Connection db, boolean overwrite) {
        try {

            db.setAutoCommit(false);
            PreparedStatement statement;

            if (overwrite) {
                // TODO: Tabellen automatisch aus hub.sql lesen
                String[] tables = {"user", "settings", "quota", "quota_ranking_criteria",
                    "allocation_rule", "course", "journal_record", "qualification",
                    "application", "evaluation", "rank"};
                for (String table : tables) {
                    statement = db.prepareStatement(
                        String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", table));
                    statement.executeUpdate();
                }
            }

            InputStreamReader reader = new InputStreamReader(
                ApplicationService.class.getResourceAsStream("/hub.sql"));
            StringBuilder str = new StringBuilder();
            char[] buffer = new char[4096];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                str.append(buffer, 0, n);
            }
            String sql = str.toString();

            try {
                statement = db.prepareStatement(sql);
                statement.execute();
            } catch (SQLException e) {
                // Syntax Error or Access Rule Violation
                if (e.getSQLState().startsWith("42")) {
                    db.rollback();
                    db.setAutoCommit(true);
                    throw new IllegalStateException("database_not_empty");
                } else {
                    throw e;
                }
            }

            db.commit();
            db.setAutoCommit(true);

        } catch (IOException e) {
            throw new IOError(e);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    // TODO: dokumentieren
    public static void setupStorage(Connection db) {
        ApplicationService.setupStorage(db, false);
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
            journal.record(ACTION_TYPE_USER_CREATED, null, null, id);
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
                throw new ObjectNotFoundException(id);
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
            throw new ObjectNotFoundException(id);
        }

        try {
            PreparedStatement statement = this.db.prepareStatement(
                String.format("SELECT * FROM \"%s\" WHERE id = ?", typeId));
            statement.setString(1, id);
            ResultSet results = statement.executeQuery();
            if (!results.next()) {
                throw new ObjectNotFoundException(id);
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
                throw new ObjectNotFoundException(id);
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
                throw new ObjectNotFoundException(id);
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
            journal.record(ACTION_TYPE_COURSE_CREATED, null, HubObject.getId(agent),
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
                throw new ObjectNotFoundException(id);
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
                throw new ObjectNotFoundException(id);
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
                throw new ObjectNotFoundException(id);
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
