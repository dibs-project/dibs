/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import static de.huberlin.cms.hub.ui.Util.checkContainsRequired;
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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.mvc.Viewable;

import de.huberlin.cms.hub.Application;
import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.HubException.IllegalStateException;
import de.huberlin.cms.hub.HubException.ObjectNotFoundException;
import de.huberlin.cms.hub.Session;
import de.huberlin.cms.hub.User;

/**
 * @author Sven Pfaller
 */
@Path("/")
@Produces("text/html")
public class Pages implements Closeable {
    private Connection db;
    private ApplicationService service;
    private User user;
    private Session session;
    private HashMap<String, Object> model;

    public Pages(@Context Configuration config, @Context CloseableService closeables,
            @CookieParam("session") Cookie sessionCookie) {
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

        this.session = null;
        if (sessionCookie != null) {
            try {
                this.session = this.service.getSession(sessionCookie.getValue());
                if (!this.session.isValid()) {
                    this.session = null;
                }
            } catch (ObjectNotFoundException e) {
                // ignore
            }
        }

        this.user = null;
        if (this.session != null) {
            this.user = this.session.getUser();
        }

        this.model = new HashMap<String, Object>();
        this.model.put("service", this.service);
        this.model.put("user", this.user);
        this.model.put("session", this.session);
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

    /* Index */

    @GET
    public Response index() {
        // TODO: redirect via filter
        if (this.user == null) {
            return Response.seeOther(UriBuilder.fromUri("/login/").build()).build();
        }

        this.model.put("applications", user.getApplications());
        return Response.ok().entity(new Viewable("/index.ftl", this.model)).build();
    }

    /* Login */

    @GET
    @Path("login")
    public Viewable login() {
        return this.login((MultivaluedMap<String, String>) null, null);
    }

    @POST
    @Path("login")
    public Response login(@Context HttpServletRequest request,
            MultivaluedMap<String, String> form) {
        try {
            checkContainsRequired(form,
                new HashSet<String>(Arrays.asList("email", "password")));

            String credential = String.format("%s:%s", form.getFirst("email"),
                form.getFirst("password"));
            Session session = this.service.login(credential, request.getRemoteHost());
            if (session == null) {
                throw new IllegalArgumentException("email_password_bad");
            }

            NewCookie cookie = new NewCookie("session", session.getId(), "/", null,
                Cookie.DEFAULT_VERSION, null, -1, session.getEndTime(), false, true);
            return Response.seeOther(UriBuilder.fromUri("/").build()).cookie(cookie)
                .build();

        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(this.login(form, e)).build();
        }
    }

    private Viewable login(MultivaluedMap<String, String> form,
            IllegalArgumentException formError) {
        this.model.put("form", form);
        this.model.put("formError", formError);
        return new Viewable("/login.ftl", this.model);
    }

    /* Logout */

    @POST
    @Path("logout")
    public Response logout() {
        ResponseBuilder response = Response
            .seeOther(UriBuilder.fromUri("/login/").build());
        if (this.session != null) {
            this.service.logout(this.session);
            NewCookie cookie = new NewCookie("session", this.session.getId(), "/", null,
                Cookie.DEFAULT_VERSION, null, -1, this.session.getEndTime(), false, true);
            response.cookie(cookie);
        }
        return response.build();
    }

    /* Register */

    @GET
    @Path("register")
    public Viewable register() {
        return this.register(null, null);
    }

    @POST
    @Path("register")
    public Response register(MultivaluedMap<String, String> form) {
        try {
            checkContainsRequired(form,
                new HashSet<String>(Arrays.asList("name", "email", "password")));

            String email = form.getFirst("email");
            String credential = String.format("%s:%s", email, form.getFirst("password"));
            this.service.register(form.getFirst("name"), email, credential);
            return Response.seeOther(UriBuilder.fromUri("/login/").build()).build();

        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(this.register(form, e)).build();
        }
    }

    private Viewable register(MultivaluedMap<String, String> form,
            IllegalArgumentException formError) {
        this.model.put("form", form);
        this.model.put("formError", formError);
        return new Viewable("/register.ftl", this.model);
    }

    /* Application */

    @GET
    @Path("applications/{id}")
    public Viewable application(@PathParam("id") String id) {
        Application application = this.service.getApplication(id);
        this.model.put("application", application);
        this.model.put("applicant", application.getUser());
        this.model.put("course", application.getCourse());
        return new Viewable("/application.ftl", this.model);
    }

    /* Courses */

    @GET
    @Path("courses")
    public Viewable courses() {
        return new Viewable("/courses.ftl", this.model);
    }

    /* Course */

    @GET
    @Path("courses/{id}")
    public Viewable course(@PathParam("id") String id,
            @QueryParam("error") String error) {
        Course course = this.service.getCourse(id);
        this.model.put("course", course);
        this.model.put("applications", course.getApplications());
        if (error != null && error.equals("course_has_applications")) {
            this.model.put("notification",
                "Die Veröffentlichung kann nicht zurückgezogen werden solange es Bewerbungen auf diesen Studiengang gibt.");
        }
        return new Viewable("/course.ftl", this.model);
    }

    /* Course.apply */

    @POST
    @Path("courses/{id}/apply")
    public Response apply(@PathParam("id") String id) {
        Course course = this.service.getCourse(id);
        URI url = null;
        try {
            Application application = course.apply(this.user.getId(), this.user);
            url = UriBuilder.fromUri("/applications/{id}/").build(application.getId());
        } catch (IllegalStateException e) {
            // TODO: user_not_connected: redirect to User.connectToDosv
            // course_not_published is handled by 404 after redirect
            url = UriBuilder.fromUri("/courses/{id}/").build(id);
        }
        return Response.seeOther(url).build();
    }

    /* Course.publish */

    @POST
    @Path("courses/{id}/publish")
    public Response coursePublish(@PathParam("id") String id) {
        // TODO: handle course_incomplete error
        Course course = this.service.getCourse(id);
        course.publish(this.user);
        return Response.seeOther(UriBuilder.fromUri("/courses/{id}/").build(id)).build();
    }

    /* Course.unpublish */

    @POST
    @Path("courses/{id}/unpublish")
    public Response courseUnpublish(@PathParam("id") String id) {
        Course course = this.service.getCourse(id);
        UriBuilder url = UriBuilder.fromUri("/courses/{id}").resolveTemplate("id", id);
        try {
            course.unpublish(this.user);
        } catch (IllegalStateException e) {
            url.queryParam("error", e.getCode());
        }
        return Response.seeOther(url.build()).build();
    }

    /* Create course */

    @GET
    @Path("create-course")
    public Viewable createCourse() {
        return this.createCourse(null, null);
    }

    @POST
    @Path("create-course")
    public Response createCourse(MultivaluedMap<String, String> form) {
        try {
            checkContainsRequired(form,
                new HashSet<String>(Arrays.asList("name", "capacity")));

            int capacity = Integer.parseInt(form.getFirst("capacity"));
            Course course = this.service.createCourse(form.getFirst("name"), capacity,
                form.containsKey("dosv"), user);
            // NOTE the first prototype does not feature a frontend for AllocationRule and
            // Quota creation.
            course.createAllocationRule(this.user)
                .createQuota("Performance", 100, this.user)
                .addRankingCriterion("qualification", this.user);
            URI url = UriBuilder.fromUri("/courses/{id}/").build(course.getId());
            return Response.seeOther(url).build();

        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(this.createCourse(form, e)).build();
        }
    }

    private Viewable createCourse(MultivaluedMap<String, String> form,
            IllegalArgumentException formError) {
        this.model.put("form", form);
        this.model.put("formError", formError);
        return new Viewable("/create-course.ftl", this.model);
    }
}
