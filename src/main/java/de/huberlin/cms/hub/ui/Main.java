/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.CloseableService;

import de.huberlin.cms.hub.ApplicationService;

@Path("/")
@Produces("text/plain")
public class Main implements Closeable {
    private Connection db;
    private ApplicationService service;

    public Main(@Context Configuration config, @Context CloseableService closeables) {
        // NOTE: can be optimized a lot

        closeables.add(this);

        try {
            this.db = DriverManager.getConnection((String) config.getProperty("db_url"),
                (String) config.getProperty("db_user"),
                (String) config.getProperty("db_password"));
        } catch (SQLException e) {
            throw new IOError(e);
        }

        Properties p = new Properties();
        p.putAll(config.getProperties());
        this.service = new ApplicationService(this.db, p);
    }

    @Override
    public void close() throws IOException {
        try {
            this.db.close();
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @GET
    public String index() {
        return "Hello World! " + this.service.toString();
    }

    @GET
    @Path("applications/{id}")
    public String application(@PathParam("id") String id) {
        return "foo " + id + " bar";
    }
}
