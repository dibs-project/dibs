/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

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
 * Aufräumen) der Testumgebung. Für Tests werden eine Datenbankverbindung und das
 * Bewerbungssystem bereitgestellt.
 * <p>
 * Die Konfiguration der Testumgebung wird aus der Datei <code>test.properties</code>
 * gelesen.
 *
 * @author Sven Pfaller
 */
public abstract class HubTest {
    /**
     * Konfiguration der Testumgebung.
     *
     * @see <code>test.default.properties</code>
     */
    protected Properties config;

    /**
     * Verwendete Datenbankverbindung.
     */
    protected Connection db;

    /**
     * Bewerbungssystem.
     */
    protected ApplicationService service;

    @Before
    public void commonBefore() throws IOException, SQLException {
        this.config = new Properties();
        this.config.load(new FileInputStream("test.default.properties"));
        try {
            this.config.load(new FileInputStream("test.properties"));
        } catch (FileNotFoundException e) {
            // ignorieren
        }

        this.db = ApplicationService.openDatabase(this.config);
        this.service = new ApplicationService(this.db, this.config);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }
}
