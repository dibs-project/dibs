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
import java.util.HashMap;
import java.util.HashSet;
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
import org.glassfish.jersey.server.mvc.Viewable;

import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.User;

/**
 * @author Sven Pfaller
 */
@Path("/")
@Produces("text/html")
public class Pages implements Closeable {

    private static final Set<String> CREATE_COURSE_FORM_KEYS =
        new HashSet<String>(Arrays.asList("name", "capacity"));

    private Connection db;
    private ApplicationService service;
    private User agent;
    private HashMap<String, Object> model;

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

        this.model = new HashMap<String, Object>();
        this.model.put("service", this.service);
        this.model.put("agent", this.agent);
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
    public Viewable index() {
        return new Viewable("/index.ftl", this.model);
    }

    @GET
    @Path("courses")
    public Viewable courses() {
        return new Viewable("/courses.ftl", this.model);
    }

    @GET
    @Path("courses/{id}")
    public Viewable course(@PathParam("id") String id) {
        this.model.put("course", this.service.getCourse(id));
        return new Viewable("/course.ftl", this.model);
    }

    @GET
    @Path("create-course")
    public Viewable createCourse() {
        return this.createCourse(null, null);
    }

    @POST
    @Path("create-course")
    public Response createCourse(MultivaluedMap<String, String> form) {
        if (!form.keySet().containsAll(CREATE_COURSE_FORM_KEYS)) {
            throw new BadRequestException();
        }

        try {
            int capacity = Integer.parseInt(form.getFirst("capacity"));
            Course course = this.service.createCourse(form.getFirst("name"), capacity,
                "cs", "bsc", this.agent);
            URI url = UriBuilder.fromUri("/courses/{id}").build(course.getId());
            return Response.seeOther(url).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(this.createCourse(form, e)).build();
        }
    }

    private Viewable createCourse(MultivaluedMap<String, String> form,
            IllegalArgumentException error) {
        this.model.put("form", form);
        this.model.put("error", error);
        return new Viewable("/create-course.ftl", this.model);
    }
}
