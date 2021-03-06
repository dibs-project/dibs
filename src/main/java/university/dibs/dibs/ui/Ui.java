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

package university.dibs.dibs.ui;

import static org.apache.commons.collections4.MapUtils.toProperties;

import university.dibs.dibs.ApplicationService;
import university.dibs.dibs.DibsException.IllegalStateException;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;

/**
 * @author Sven Pfaller
 */
public class Ui extends ResourceConfig {
    private static Logger logger = Logger.getLogger(Ui.class.getPackage().getName());

    /**
     * TODO.
     *
     * @param servletContext TODO
     */
    public Ui(@Context ServletContext servletContext) {
        // Jersey konfigurieren
        this.packages(this.getClass().getPackage().getName());
        this.register(FreemarkerMvcFeature.class);
        this.property(FreemarkerMvcFeature.TEMPLATES_BASE_PATH, "/templates");

        // Standardwerte der Konfiguration laden
        try {
            Properties defaults = new Properties();
            defaults.load(this.getClass().getResourceAsStream("/default.properties"));
            for (String name : defaults.stringPropertyNames()) {
                this.property(name, defaults.getProperty(name));
            }
        } catch (IOException e) {
            throw new IOError(e);
        }

        // Konfiguration aus Servlet-Kontextparametern übernehmen
        Enumeration<String> names = servletContext.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            this.property(name, servletContext.getInitParameter(name));
        }

        // setup database
        Connection db;
        try {
            // manually load driver, because it is not guaranteed that the server's
            // classloader supports SPI
            Class.forName("org.postgresql.Driver");
            db = DriverManager.getConnection((String) this.getProperty("db_url"),
                (String) this.getProperty("db_user"),
                (String) this.getProperty("db_password"));
        } catch (SQLException e) {
            logger.severe("failed to connect to database");
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            // unreachable
            throw new RuntimeException(e);
        }

        logger.info("setting up storage…");
        final Properties config = toProperties(Ui.this.getProperties());
        try {
            ApplicationService.setupStorage(db);
            logger.info("storage set up");
        } catch (IllegalStateException e) {
            logger.info("storage already set up");
        }

        try {
            db.close();
        } catch (SQLException e) {
            throw new IOError(e);
        }

        /* Set up timer task for DoSV synchronisation */
        if (config.getProperty("dosv_sync_enabled").equals("false")) {
            return;
        }
        Long interval =
            Long.parseLong((String) this.getProperty("dosv_sync_interval")) * 1000 * 60;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Connection db;
                try {
                    db = DriverManager.getConnection(config.getProperty("db_url"),
                        config.getProperty("db_user"),
                        config.getProperty("db_password"));
                } catch (SQLException e) {
                    throw new IOError(e);
                }

                new ApplicationService(db, config).getDosvSync().synchronize();
                logger.info("DoSV synchronized");

                try {
                    db.close();
                } catch (SQLException e) {
                    throw new IOError(e);
                }
            }
        }, interval, interval);
    }

    /**
     * TODO.
     *
     * @param args TODO
     * @throws Exception TODO
     */
    public static void main(String[] args) throws Exception {
        university.dibs.dibs.Util.configureLogging(false);

        // Jetty-Logging konfigurieren
        System.setProperty("org.eclipse.jetty.util.log.class",
            "org.eclipse.jetty.util.log.JavaUtilLog");

        Properties config = new Properties();
        try {
            // TODO: Pfad zur Konfigurationsdatei aus args lesen
            config.load(new FileInputStream("dibs.properties"));
        } catch (FileNotFoundException e) {
            // ignorieren
        }

        int port = 8080;
        Server server = new Server(port);
        // TODO: Pfad zur WebApp aus args lesen
        WebAppContext webapp = new WebAppContext("src/main/webapp", "/");
        for (String name : config.stringPropertyNames()) {
            webapp.setInitParameter(name, config.getProperty(name));
        }
        server.setHandler(webapp);

        System.err.println("dibs");
        System.err.println(UriBuilder.fromUri("http://localhost:{port}/").build(port));
        server.start();
        server.join();
    }
}
