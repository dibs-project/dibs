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

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.collections4.CollectionUtils.filter;

import university.dibs.dibs.DibsException.IllegalStateException;
import university.dibs.dibs.DibsException.ObjectNotFoundException;
import university.dibs.dibs.dosv.DosvSync;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

/**
 * Repräsentiert den Bewerbungsdienst, bzw.&nbsp;den Bewerbungsprozess.
 *
 * <p>ApplicationService an sich ist nicht threadsafe, aber ein leichtgewichtiges Objekt. Soll er in
 * mehreren Threads benutzt werden (z.B. in einer Webanwendung) kann einfach für jeden Thread ein
 * eigenes Objekt erstellt werden.
 *
 * @author Sven Pfaller
 */
public class ApplicationService {
    /** Benötigte Version des Datenspeichers. */
    public static final String STORAGE_VERSION = "0";

    /** Aktionstyp: neuer Benutzer angelegt. */
    public static final String ACTION_TYPE_USER_CREATED = "user_created";
    /** Aktionstyp: Benutzer ist mit dem DoSV verbunden. */
    public static final String APPLICATION_TYPE_USER_CONNECTED_TO_DOSV = "user_connected_to_dosv";
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
    /** Action type: The admission of applicants for a course has started. */
    public static final String ACTION_TYPE_COURSE_ADMISSION_STARTED = "course_admission_started";
    /** Aktionstyp: Bewerbungstatus bearbeitet. */
    public static final String ACTION_TYPE_APPLICATION_STATUS_SET = "application_status_set";
    /** Aktionstyp: Quote erstellt und mit der Vergaberegel verknüpft. */
    public static final String ACTION_TYPE_ALLOCATION_RULE_QUOTA_CREATED =
        "allocation_rule_quota_created";
    /** Aktionstyp: Kriterium zur Sortierung von Bewerbern mit Quote verknüpft. */
    public static final String ACTION_TYPE_QUOTA_RANKING_CRITERION_ADDED =
        "quota_ranking_criterion_added";
    /** Aktionstyp: dibs wurde mit dem DoSV synchronisiert. */
    public static final String ACTION_TYPE_DOSV_SYNC_SYNCHRONIZED =
        "dosv_sync_synchronized";

    /** Supported filters for {@link #getCourses(Map)}. */
    public static final Set<String> GET_COURSES_FILTER_KEYS =
        new HashSet<>(Arrays.asList("published"));
    /** Supported filters for {@link #getCriteria(Map, User)}. */
    public static final Set<String> GET_CRITERIA_FILTER_KEYS =
        new HashSet<>(Arrays.asList("required_information_type_id"));

    private static final long MONTH_DURATION = 30 * 24 * 60 * 60 * 1000L;

    private Properties config;
    private Connection db;
    private Journal journal;
    private Map<String, Information.Type> informationTypes;
    private Map<String, Criterion> criteria;
    private int transactionLevel;
    private QueryRunner queryRunner;
    private DosvSync dosvSync;

    /**
     * Initialisiert den ApplicationService.
     *
     * @param db Datenbankverbindung, die verwendet werden soll.
     * @param config Konfiguration.
     * @see #getConfig()
     */
    public ApplicationService(Connection db, Properties config) {
        this.db = db;

        this.config = new Properties();
        this.config.putAll(config);
        this.journal = new Journal(this);

        this.informationTypes = new HashMap<String, Information.Type>();
        this.informationTypes.put("qualification", new Qualification.Type());

        this.criteria = new HashMap<String, Criterion>();
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("id", "qualification");
        args.put("requiredInformationType", this.informationTypes.get("qualification"));
        args.put("service", this);
        this.criteria.put("qualification", new QualificationCriterion(args));

        this.transactionLevel = -1;
        this.queryRunner = new QueryRunner();
        if ("true".equals(config.getProperty("dosv_sync_enabled"))) {
            this.dosvSync = new DosvSync(this);
        }

    }

    /**
     * Creates a new user.
     *
     * @param name name which dibs uses to address the user
     * @param email email address
     * @param credential credential
     * @param role role
     * @return new user
     * @throws DibsException.IllegalStateException if the email was already registered
     *     (<code>email_already_existing</code>).
     */
    public User createUser(String name, String email, String credential, String role) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("illegal name: empty");
        }
        if (email.isEmpty()) {
            throw new IllegalArgumentException("illegal email: empty");
        }
        // TODO: validate credential
        // TODO: validate role

        try {
            this.beginTransaction();
            String id = String.format("user:%s", new Random().nextInt());
            new QueryRunner().insert(this.getDb(),
                "INSERT INTO \"user\" VALUES(?, ?, ?, ?, ?)",
                new MapHandler(), id, name, email, credential, role);
            this.journal.record(ACTION_TYPE_USER_CREATED, null, null, id);
            this.endTransaction();
            return this.getUser(id);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                throw new IllegalStateException("email_already_existing");
            } else {
                throw new IOError(e);
            }
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
            Map<String, Object> args = this.queryRunner.query(this.db,
                "SELECT * FROM \"user\" WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new User(args);
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
            List<User> users = new ArrayList<User>();
            List<Map<String, Object>> queryResults = this.queryRunner.query(this.db,
                "SELECT * FROM \"user\"", new MapListHandler());
            for (Map<String, Object> args : queryResults) {
                args.put("service", this);
                users.add(new User(args));
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
            throw new ObjectNotFoundException(id);
        }

        try {
            Map<String, Object> args = this.queryRunner.query(this.db,
                String.format("SELECT * FROM \"%s\" WHERE id = ?", typeId),
                new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            return type.newInstance(args, this);
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
            Map<String, Object> args = this.queryRunner.query(
                this.db, "SELECT * FROM application WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new Application(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Returns all Applications in dibs.
     *
     * @return List of all Applications
     */
    public List<Application> getApplications() {
        List<Application> applications = new ArrayList<Application>();
        List<Map<String, Object>> queryResults;
        try {
            queryResults = this.queryRunner.query(this.db,
                "SELECT * FROM application", new MapListHandler());
        } catch (SQLException e) {
            throw new IOError(e);
        }
        for (Map<String, Object> args : queryResults) {
            args.put("service", this);
            applications.add(new Application(args));
        }
        return applications;
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
            Map<String, Object> args = this.queryRunner.query(
                this.db, "SELECT * FROM evaluation WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new Evaluation(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Registers a new applicant.
     *
     * @param name name which dibs uses to address the user
     * @param email email address
     * @param credential credential
     * @return new applicant
     */
    public User register(String name, String email, String credential) {
        return this.createUser(name, email, credential, User.ROLE_APPLICANT);
    }

    /**
     * TODO.
     *
     * @param credential TODO
     * @return TODO
     */
    public User authenticate(String credential) {
        try {
            Map<String, Object> args = new QueryRunner().query(this.getDb(),
                "SELECT * FROM \"user\" WHERE credential = ?", new MapHandler(),
                credential);
            if (args == null) {
                return null;
            }
            args.put("service", this);
            return new User(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * TODO.
     *
     * @param credential TODO
     * @param device TODO
     * @return TODO
     */
    public Session login(String credential, String device) {
        if (device.isEmpty()) {
            throw new IllegalArgumentException("device_empty");
        }

        User user = this.authenticate(credential);
        if (user == null) {
            return null;
        }

        String id = String.format("session:%s", new Random().nextInt());
        Timestamp startTime = new Timestamp(new Date().getTime());
        Timestamp endTime = new Timestamp(startTime.getTime() + ApplicationService.MONTH_DURATION);

        try {
            new QueryRunner().insert(this.getDb(),
                "INSERT INTO session VALUES (?, ?, ?, ?, ?)", new MapHandler(), id,
                user.getId(), device, startTime, endTime);
            return this.getSession(id);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * TODO.
     *
     * @param session TODO
     */
    public void logout(Session session) {
        session.end();
    }

    /**
     * TODO.
     *
     * @param id TODO
     * @return TODO
     */
    public Session getSession(String id) {
        try {
            Map<String, Object> args = new QueryRunner().query(this.getDb(),
                "SELECT * FROM session WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new Session(args);
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
            this.queryRunner.update(this.getDb(), "UPDATE settings SET semester = ?",
                semester);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Creates a new course.
     *
     * @param name name
     * @param capacity capacity, i.e. number of places (errors:
     *     <code>capacity_nonpositive</code>)
     * @param dosv whether this Course is using the DoSV for the admission process
     * @param agent active user
     * @return new course
     */
    public Course createCourse(String name, int capacity, boolean dosv, User agent) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name_empty");
        }
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity_nonpositive");
        }

        try {
            this.beginTransaction();
            String id = String.format("course:%s", new Random().nextInt());
            this.queryRunner.insert(this.getDb(),
                "INSERT INTO course (id, name, capacity, dosv) VALUES (?, ?, ?, ?)",
                new MapHandler(), id, name, capacity, dosv);
            this.journal.record(ACTION_TYPE_COURSE_CREATED, null, DibsObject.getId(agent), name);
            this.endTransaction();
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
            Map<String, Object> args = this.queryRunner.query(this.db,
                "SELECT * FROM course WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new Course(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * All courses in dibs.
     *
     * @see #getCourses(Map)
     */
    // overload
    public List<Course> getCourses() {
        return this.getCourses(new HashMap<String, Object>());
    }

    /**
     * All courses in dibs.
     *
     * @param filter TODO
     * @return TODO
     */
    public List<Course> getCourses(Map<String, Object> filter) {
        if (!GET_COURSES_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("filter_unknown_keys");
        }

        List<String> filterConditions = new ArrayList<>();
        List<Object> filterValues = new ArrayList<>();
        Boolean published = (Boolean) filter.get("published");
        if (published != null) {
            filterConditions.add("published = ?");
            filterValues.add(published);
        }
        String filterSql = filterConditions.isEmpty()
            ? "" : " WHERE " + StringUtils.join(filterConditions, " AND ");

        try {
            String sql = "SELECT * FROM course" + filterSql;
            List<Map<String, Object>> results = this.queryRunner.query(this.db, sql,
                new MapListHandler(), filterValues.toArray());

            List<Course> courses = new ArrayList<>();
            for (Map<String, Object> args : results) {
                args.put("service", this);
                courses.add(new Course(args));
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
            Map<String, Object> args = this.queryRunner.query(this.db,
                "SELECT * FROM allocation_rule WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new AllocationRule(args);
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
            Map<String, Object> args = this.queryRunner.query(
                this.db, "SELECT * FROM quota WHERE id = ?", new MapHandler(), id);
            if (args == null) {
                throw new ObjectNotFoundException(id);
            }
            args.put("service", this);
            return new Quota(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Returns the criterion with the specified <code>id</code>.
     *
     * @param id criterion id
     * @return retrieved criterion
     */
    public Criterion getCriterion(String id) {
        Criterion criterion = this.criteria.get(id);
        if (criterion == null) {
            throw new ObjectNotFoundException(id);
        }
        return criterion;
    }

    /**
     * All criteria in dibs.
     *
     * @see #getCriteria(Map, User)
     */
    // overload
    public List<Criterion> getCriteria(User agent) {
        return this.getCriteria(new HashMap<String, Object>(), agent);
    }

    /**
     * All criteria in dibs.
     *
     * @param filter TODO
     * @param agent active user
     * @return TODO
     */
    public List<Criterion> getCriteria(Map<String, Object> filter, User agent) {
        if (!GET_CRITERIA_FILTER_KEYS.containsAll(filter.keySet())) {
            throw new IllegalArgumentException("filter_unknown_keys");
        }

        List<Criterion> criteria = new ArrayList<>(this.criteria.values());
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
     * Begins a database transaction. Has to be used in conjunction with {@link #endTransaction}.
     */
    public void beginTransaction() {
        if (this.transactionLevel == -1) {
            try {
                this.db.setAutoCommit(false);
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
        this.transactionLevel++;
    }

    /**
     * Ends a database transaction and commits if it is the root transaction.
     * Has to be used in conjunction with {@link #beginTransaction}.
     *
     * @throws IllegalStateException if no transaction is open
     *     (code: <code>transaction_not_open</code>).
     */
    public void endTransaction() {
        if (this.transactionLevel == -1) {
            throw new IllegalStateException("transaction_not_open");
        }

        this.transactionLevel--;
        if (this.transactionLevel == -1) {
            try {
                this.db.commit();
                this.db.setAutoCommit(true);
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
    }

    /* ---- Properties ---- */

    /**
     * Konfiguration. Die Einstellungen sind in <code>default.properties</code> dokumentiert.
     */
    public Properties getConfig() {
        return this.config;
    }

    /**
     * Gibt die Einstellungen des Bewerbungssystems zurück.
     *
     * @return Einstellungen des Bewerbungssystems
     */
    public Settings getSettings() {
        try {
            Map<String, Object> args = this.queryRunner.query(this.db,
                "SELECT * FROM settings", new MapHandler());
            args.put("service", this);
            return new Settings(args);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    /**
     * Verfügbare Informationstypen (indiziert nach ID).
     */
    public Map<String, Information.Type> getInformationTypes() {
        return unmodifiableMap(this.informationTypes);
    }

    /**
     * Das Protokollbuch des Bewerbungsdienstes.
     */
    public Journal getJournal() {
        return new Journal(this);
    }

    /**
     * Verwendete Datenbankverbindung.
     */
    public Connection getDb() {
        return this.db;
    }

    /**
     * Führt Datenbankabfragen aus. Das Abfrageergebnis wird mit Hilfe des
     * <code>ResultSetHandler</code> in eine <code>Map</code> oder eine Liste von Maps umgewandelt.
     */
    public QueryRunner getQueryRunner() {
        return this.queryRunner;
    }

    /**
     * DoSV synchronisation class. Is <code>null</code> if not enabled.
     */
    public DosvSync getDosvSync() {
        return this.dosvSync;
    }

    /* ---- /Properties ---- */

    /**
     * TODO.
     */
    // overload
    public static void setupStorage(Connection db) {
        ApplicationService.setupStorage(db, false);
    }

    /**
     * TODO.
     *
     * @param db TODO
     * @param overwrite TODO
     */
    public static void setupStorage(Connection db, boolean overwrite) {
        try {

            db.setAutoCommit(false);

            if (overwrite) {
                // TODO: Tabellen automatisch aus dibs.sql lesen
                String[] tables = {"user", "settings", "quota", "quota_ranking_criteria",
                    "allocation_rule", "course", "journal_record", "qualification",
                    "application", "evaluation", "rank", "session"};
                for (String table : tables) {
                    new QueryRunner().update(
                        db, String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", table));
                }
            }

            InputStreamReader reader = new InputStreamReader(
                ApplicationService.class.getResourceAsStream("/dibs.sql"));
            StringBuilder str = new StringBuilder();
            // checkstyle: ignore MagicNumber, only used here
            char[] buffer = new char[4096];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                str.append(buffer, 0, n);
            }
            String sql = str.toString();

            try {
                PreparedStatement statement = db.prepareStatement(sql);
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

        // TODO: recursive transaction
        ApplicationService service = new ApplicationService(db, new Properties());
        service.createUser("Administrator", "admin", "admin:admin", User.ROLE_ADMIN);
    }
}
