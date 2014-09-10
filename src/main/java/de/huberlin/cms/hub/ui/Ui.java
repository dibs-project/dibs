/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.mvc.freemarker.FreemarkerMvcFeature;

import de.huberlin.cms.hub.ApplicationService;

public class Ui extends ResourceConfig {
    private static Logger logger = Logger.getLogger(Ui.class.getPackage().getName());

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

        // Datenbank einrichten
        Connection db;
        try {
            db = DriverManager.getConnection((String) this.getProperty("db_url"),
                (String) this.getProperty("db_user"),
                (String) this.getProperty("db_password"));
        } catch (SQLException e) {
            logger.severe("failed to connect to database");
            throw new RuntimeException(e);
        }

        logger.info("setting up database…");
        try {
            ApplicationService.setupDatabase(db);
            logger.info("database set up");
        } catch (IllegalStateException e) {
            logger.info("database already set up");
        }

        try {
            db.close();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    public static void main(String[] args) throws Exception {
        // Logging konfigurieren
        System.setProperty("java.util.logging.SimpleFormatter.format",
            "%1$tT %4$s %3$s: %5$s%6$s%n");
        LogManager.getLogManager().reset();
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.FINEST);
        Logger.getLogger("").addHandler(handler);

        // Jetty-Logging konfigurieren
        System.setProperty("org.eclipse.jetty.util.log.class",
            "org.eclipse.jetty.util.log.JavaUtilLog");
        Logger jettyLogger = Logger.getLogger("org.eclipse.jetty");
        jettyLogger.setLevel(Level.INFO);

        // Jersey-Logging konfigurieren
        Logger jerseyTracingLogger =
            Logger.getLogger("org.glassfish.jersey.tracing.general");
        jerseyTracingLogger.setLevel(Level.FINE);

        Properties config = new Properties();
        try {
            // TODO: Pfad zur Konfigurationsdatei aus args lesen
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // ignorieren
        }

        int port = 8080;
        Server server = new Server(port);
        // TODO: WebApp-Pfad konfigurierbar machen
        WebAppContext webapp = new WebAppContext("src/main/webapp", "/");
        for (String name : config.stringPropertyNames()) {
            webapp.setInitParameter(name, config.getProperty(name));
        }
        server.setHandler(webapp);

        // TODO: HUB-Version ausgeben
        System.err.println("HUB");
        System.err.println(UriBuilder.fromUri("http://localhost:{port}/").build(port));
        server.start();
        server.join();
    }
}
