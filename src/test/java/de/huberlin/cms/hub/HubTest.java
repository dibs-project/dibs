/*
 * HUB
 */

package de.huberlin.cms.hub;

import static org.junit.Assume.assumeTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;

/**
 * Basisklasse für HUB-Tests. Übernimmt die Initialisierung der Testumgebung.
 *
 * @author Sven Pfaller <sven.pfaller AT hu-berlin.de>
 */
public class HubTest {
    /**
     * ID eines allgemeinen Bewerbers.
     */
    public final static int APPLICANT_ID = 100;

    /**
     * Verwendete Datenbankverbindung.
     */
    protected Connection db;

    /**
     * Bewerbungssystem zum Testen.
     */
    protected ApplicationService service;

    @Before
    public void commonBefore() throws IOException, SQLException {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // skip the tests
            assumeTrue(false);
        }

        this.db = ApplicationService.openDatabase(config);
        service = new ApplicationService(this.db, config);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }
}
