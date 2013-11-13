/*
 * hub
 */

package de.huberlin.cms.hub;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Repräsentiert den Bewerbungsdienst, bzw. den Bewerbungsprozess.
 * <p>
 * ApplicationService an sich ist nicht threadsafe, aber ein leichtgewichtiges Objekt.
 * Soll er in mehreren Threads benutzt werden (z.B. in einer Webanwendung) kann einfach
 * für jeden Thread ein eigenes Objekt erstellt werden.
 */
public class ApplicationService {
    protected Connection db;
    protected Properties config;

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
        db.setAutoCommit(false);
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
}
