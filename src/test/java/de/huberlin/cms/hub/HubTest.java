/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 * Basisklasse für HUB-Tests. Übernimmt die Initialisierung (und das anschließende
 * Aufräumen) der Testumgebung. Für Tests werden eine Datenbankverbindung, das
 * Bewerbungssystem und ein allgemeiner Benutzer bereitgestellt.
 * <p>
 * Die Konfiguration der Testumgebung wird aus der Datei <code>test.properties</code>
 * gelesen.
 *
 * @author Sven Pfaller
 */
public abstract class HubTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    /**
     * Allgemeiner Benutzer.
     */
    protected User user;

    /**
     * Allgemeiner Studiengang.
     */
    protected Course course;

    @Before
    public void commonBefore() throws IOException, SQLException {
        this.config = new Properties();
        this.config.load(new FileInputStream("test.default.properties"));
        try {
            this.config.load(new FileInputStream("test.properties"));
        } catch (FileNotFoundException e) {
            // ignorieren
        }

        this.db = DriverManager.getConnection(config.getProperty("db_url"),
            config.getProperty("db_user"),
            config.getProperty("db_password"));
        ApplicationService.setupStorage(this.db, true);

        this.service = new ApplicationService(this.db, this.config);
        this.user = this.service.createUser("Jen", "barber@example.org");

        String randomStr = Integer.toString(new Random().nextInt());
        course = service.createCourse("test-" + randomStr, 500, randomStr, "degree", null);
        this.course.createAllocationRule(null).createQuota("Standard", 100, null).
            addRankingCriterion("qualification", null);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }
}
