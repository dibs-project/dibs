/*
 * HUB
 */

package de.huberlin.cms.hub;

import static org.junit.Assume.assumeTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

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
        service = new ApplicationService(ApplicationService.openDatabase(config), config);
    }
}
