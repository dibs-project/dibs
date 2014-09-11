/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import static org.apache.commons.collections4.MapUtils.toProperties;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.server.CloseableService;

import de.huberlin.cms.hub.ApplicationService;

/**
 * @author Sven Pfaller
 */
@Path("/")
@Produces("text/plain")
public class Pages implements Closeable {
    private Connection db;
    private ApplicationService service;

    public Pages(@Context Configuration config, @Context CloseableService closeables) {
        closeables.add(this);

        // NOTE: kann deutlich optimiert werden
        try {
            this.db = DriverManager.getConnection((String) config.getProperty("db_url"),
                (String) config.getProperty("db_user"),
                (String) config.getProperty("db_password"));
        } catch (SQLException e) {
            throw new IOError(e);
        }

        this.service =
            new ApplicationService(this.db, toProperties(config.getProperties()));
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.db != null) {
                this.db.close();
            }
        } catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @GET
    public String index() {
        return "HUB\n";
    }

    @GET
    @Path("courses")
    public String courses() {
        return this.service.getCourses().toString();
    }
}
