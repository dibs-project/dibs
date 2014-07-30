/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class Ui extends ResourceConfig {
    public Ui() {
        this.packages(this.getClass().getPackage().getName());
    }

    public static void main(String[] args) throws IOException {
        // TODO: args besser validieren

        String configPath = null;
        if (args.length >= 1) {
            configPath = args[0];
        }

        Properties config = new Properties();
        config.load(Ui.class.getResourceAsStream("/default.properties"));
        if (configPath != null) {
            config.load(new FileInputStream(args[0]));
        }

        URI url = UriBuilder.fromUri("http://localhost:8080/").build();
        Ui ui = new Ui();
        for (String name : config.stringPropertyNames()) {
            ui.property(name, config.getProperty(name));
        }

        System.err.println("HUB 0.1.0");
        System.err.println(url.toString());
        JettyHttpContainerFactory.createServer(url, ui);
    }
}
