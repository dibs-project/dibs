/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import static org.apache.commons.collections4.MapUtils.toProperties;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.CloseableService;

import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.User;

/**
 * @author Sven Pfaller
 */
@Path("/")
@Produces("text/plain")
public class Pages implements Closeable {

    private static final Set<String> CREATE_COURSE_FORM_KEYS =
        new HashSet<String>(Arrays.asList("name", "capacity"));

    private Connection db;
    private ApplicationService service;
    private User agent;

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
        this.agent = null;
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
        return this.service.getCourses().toString() + "\n";
    }

    @GET
    @Path("courses/{id}")
    public String course(@PathParam("id") String id) {
        return this.service.getCourse(id).toString() + "\n";
    }

    @GET
    @Path("create-course")
    public String createCourse() {
        return this.createCourse((IllegalArgumentException) null);
    }

    @POST
    @Path("create-course")
    public Response createCourse(MultivaluedMap<String, String> form) {
        if (!form.keySet().containsAll(CREATE_COURSE_FORM_KEYS)) {
            throw new BadRequestException();
        }

        try {
            int capacity = Integer.parseInt(form.getFirst("capacity"));
            Course course =
                this.service.createCourse(form.getFirst("name"), capacity, this.agent);
            URI url = UriBuilder.fromUri("/courses/{id}").build(course.getId());
            return Response.seeOther(url).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(this.createCourse(e)).build();
        }
    }

    private String createCourse(IllegalArgumentException error) {
        return String.format("create-course-view\n%s",
            error != null ? error.toString() + "\n" : "");
    }
}
