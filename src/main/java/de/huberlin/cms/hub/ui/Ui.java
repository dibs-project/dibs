/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class Ui extends ResourceConfig {
    public Ui() {
        // TODO: Tracing konfigurierbar machen
        this.property(ServerProperties.TRACING, "ALL");
        this.packages(this.getClass().getPackage().getName());
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
        config.load(Ui.class.getResourceAsStream("/default.properties"));
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
            webapp.setAttribute(name, config.getProperty(name));
        }
        server.setHandler(webapp);

        // TODO: HUB-Version ausgeben
        System.err.println("HUB");
        System.err.println(UriBuilder.fromUri("http://localhost:{port}/").build(port));
        server.start();
        server.join();
    }
}
