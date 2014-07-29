/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

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

        this.db = ApplicationService.openDatabase(this.config);
        this.initDatabase();

        this.service = new ApplicationService(this.db, this.config);
        this.user = this.service.createUser("Jen", "barber@example.org");

        this.course = this.service.createCourse("Computer Science", 500, null);
        this.course.createAllocationRule(null).createQuota("Standard", 100, null).
            addRankingCriterion("qualification", null);
    }

    @After
    public void commonAfter() throws SQLException {
        if (this.db != null) {
            this.db.close();
        }
    }

    private void initDatabase() {
        try {

            this.db.setAutoCommit(false);
            PreparedStatement statement;

            // TODO: Tabellen automatisch aus hub.sql lesen
            String[] tables = {"user", "settings", "quota", "quota_ranking_criteria",
                "allocation_rule", "course", "journal_record", "qualification", "application"};
            for (String table : tables) {
                statement = this.db.prepareStatement(
                    String.format("DROP TABLE IF EXISTS \"%s\" CASCADE", table));
                statement.executeUpdate();
            }

            InputStreamReader reader =
                new InputStreamReader(this.getClass().getResourceAsStream("/hub.sql"));
            StringBuilder str = new StringBuilder();
            char[] buffer = new char[4096];
            int n = 0;
            while ((n = reader.read(buffer)) != -1) {
                str.append(buffer, 0, n);
            }
            String sql = str.toString();

            statement = this.db.prepareStatement(sql);
            statement.execute();
            this.db.commit();
            this.db.setAutoCommit(true);

        } catch (IOException e) {
            throw new IOError(e);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
