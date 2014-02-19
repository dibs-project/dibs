/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
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
 * Basisklasse für HUB-Tests. Übernimmt die Initialisierung (und das anschließende
 * Aufräumen) der Testumgebung. Für Tests werden eine Datenbankverbindung, das
 * Bewerbungssystem und ein allgemeiner Bewerber bereitgestellt.
 * <p>
 * Die Konfiguration der Datenbank und des Bewerbungssystems wird aus der Datei
 * <code>hub.properties</code> gelesen.
 *
 * @author pfallers
 * @see ApplicationService#getConfig()
 * @see ApplicationService#openDatabase(Properties)
 */
public class HubTest {
    /**
     * Verwendete Datenbankverbindung.
     */
    protected Connection db;

    /**
     * Bewerbungssystem.
     */
    protected ApplicationService service;

    /**
     * Allgemeiner Bewerber.
     */
    protected Applicant applicant;

    @Before
    public void commonBefore() throws IOException, SQLException {
        Properties config = new Properties();
        try {
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // Tests abbrechen
            assumeTrue(false);
        }

        this.db = ApplicationService.openDatabase(config);
        this.service = new ApplicationService(this.db, config);
        this.applicant = this.service.getApplicant(100);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }
}
