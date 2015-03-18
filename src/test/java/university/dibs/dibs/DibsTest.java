/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

import university.dibs.dibs.ApplicationService;
import university.dibs.dibs.Course;
import university.dibs.dibs.User;

/**
 * Basisklasse für dibs-Tests. Übernimmt die Initialisierung (und das anschließende
 * Aufräumen) der Testumgebung. Für Tests werden eine Datenbankverbindung, das
 * Bewerbungssystem und ein allgemeiner Benutzer bereitgestellt.
 * <p>
 * Die Konfiguration der Testumgebung wird aus der Datei <code>test.properties</code>
 * gelesen.
 *
 * @author Sven Pfaller
 */
public abstract class DibsTest {
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
     * Administrator.
     */
    protected User admin;

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
        this.admin = this.service.getUsers().get(0);
        this.user = this.service.createUser("Jen", "barber@example.org",
            "barber@example.org:secr3t", User.ROLE_APPLICANT);

        this.course = this.service.createCourse("Computer Science", 500, false, null);
        this.course.createAllocationRule(null).createQuota("Standard", 100, null).
            addRankingCriterion("qualification", null);
        this.course.publish(null);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }
}
