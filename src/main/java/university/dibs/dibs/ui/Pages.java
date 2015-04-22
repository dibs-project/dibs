/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs.ui;

import static org.apache.commons.collections4.MapUtils.toProperties;
import static university.dibs.dibs.ui.Util.checkContainsRequired;

import java.io.Closeable;
import java.io.IOError;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
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
import javax.ws.rs.core.UriInfo;
import javax.xml.ws.WebServiceException;

import org.glassfish.jersey.server.CloseableService;
import org.glassfish.jersey.server.mvc.Viewable;

import university.dibs.dibs.Application;
import university.dibs.dibs.ApplicationService;
import university.dibs.dibs.Course;
import university.dibs.dibs.DibsException;
import university.dibs.dibs.DibsException.IllegalStateException;
import university.dibs.dibs.DibsException.ObjectNotFoundException;
import university.dibs.dibs.Information;
import university.dibs.dibs.Session;
import university.dibs.dibs.User;

// TODO put private "overloaded" methods directly after their public counterparts
/**
 * @author Sven Pfaller
 * @author Markus Michler
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
            @Context UriInfo urlInfo, @CookieParam("session") Cookie sessionCookie) {
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
        this.model.put("url", urlInfo.getRequestUri());
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

    /* User.createInformation */

    @GET
    @Path("users/{id}/create-information")
    public Viewable createInformation(@PathParam("id") String id, @QueryParam("type")
            String typeId) {
        Information.Type type = service.getInformationTypes().get(typeId);
        if (type == null) {
            throw new NotFoundException();
        }
        return createInformation(id, type.getId(), null, null);
    }

    private Viewable createInformation(String userId, String typeId,
            MultivaluedMap<String, String> form, IllegalArgumentException formError) {
        model.put("type", typeId);
        model.put("form", form);
        model.put("formError", formError);
        return new Viewable(String.format("/user-create-information-%s.ftl", typeId), model);
    }

    @POST
    @Path("users/{id}/create-information")
    public Response createInformation(@PathParam("id") String id, @QueryParam("type")
            String typeId, MultivaluedMap<String, String> form) {
        Information.Type type = service.getInformationTypes().get(typeId);
        if (type == null) {
            throw new NotFoundException();
        }
        try {
            // NOTE hard-coded for qualification
            checkContainsRequired(form, new HashSet<String>(Arrays.asList("grade")));
            double grade;
            try {
                grade = new Double(form.getFirst("grade"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("grade_nan");
            }

            Map<String, Object> args = new HashMap<>();
            args.put("grade", grade);
            Information information = user.createInformation(type.getId(), args, user);

            URI url = UriBuilder.fromUri("information/{id}/").build(information.getId());

            return Response.seeOther(url).build();
        } catch (IllegalArgumentException e) {
            return Response.status(400).entity(createInformation(id, type.getId(), form, e))
                .build();
        }
    }

    /* User.connectToDosv */

    @GET
    @Path("users/{id}/connect-to-dosv")
    public Viewable userConnectToDosv(@QueryParam("course-id") String courseId) {
        return this.userConnectToDosv(courseId, null, null, null);
    }

    @POST
    @Path("users/{id}/connect-to-dosv")
    public Response userConnectToDosv(@QueryParam("course-id") String courseId,
            MultivaluedMap<String, String> form) {
        try {
            Util.checkContainsRequired(form,
                new HashSet<>(Arrays.asList("dosv-bid", "dosv-ban")));

            boolean connected = this.user.connectToDosv(form.getFirst("dosv-bid"),
                form.getFirst("dosv-ban"), this.user);
            if (connected == false) {
                throw new IllegalArgumentException("dosv_bid_dosv_ban_bad");
            }

            URI url = null;
            if (courseId == null) {
                // TODO redirect to /users/{id}
                url = UriBuilder.fromUri("/").build();
            } else {
                url = UriBuilder.fromUri("/courses/{id}?post=user-connect-to-dosv")
                    .build(courseId);
            }
            return Response.seeOther(url).build();

        } catch (IllegalArgumentException | WebServiceException e) {
            IllegalArgumentException formError = null;
            Exception error = null;
            if (e instanceof IllegalArgumentException) {
                formError = (IllegalArgumentException) e;
            } else {
                error = e;
            }
            return Response.status(400)
                .entity(this.userConnectToDosv(courseId, form, formError, error)).build();
        }
    }

    private Viewable userConnectToDosv(@QueryParam("course-id") String courseId,
            MultivaluedMap<String, String> form, IllegalArgumentException formError,
            Exception error) {
        this.model.put("form", form);
        this.model.put("formError", formError);
        if (courseId != null) {
            this.model.put("notification",
                "Um dich auf einen Studiengang zu bewerben, dessen Zulassung über hochschulstart.de läuft, musst du erst dein Konto mit hochschulstart.de verbinden.");
        }
        // WebServiceException
        if (error != null) {
            this.model.put("notification",
                "Konnte wegen einer technischen Störung keine Verbindung mit hochschulstart.de herstellen. Bitte versuche es in ein paar Minuten noch ein mal.");
        }
        return new Viewable("/user-connect-to-dosv.ftl", this.model);
    }

    /* Information */

    @GET
    @Path("information/{id}/")
    public Viewable information(@PathParam("id") String id) {
        Information information = service.getInformation(id);

        model.put("information", information);

        // TODO introduce conditional for different Information.Types
        return new Viewable("/information-qualification.ftl", model);
    }

    /* Application */

    @GET
    @Path("applications/{id}")
    public Viewable application(@PathParam("id") String id) {
        Application application = this.service.getApplication(id);
        this.model.put("application", application);
        this.model.put("applicant", application.getUser());
        this.model.put("course", application.getCourse());

        List<Information.Type> requiredInformationTypes = new ArrayList<>();
        // TODO replace hardcoded list with backend method
        requiredInformationTypes.add(service.getInformationTypes().get("qualification"));

        Map<String, Information> requiredInformationMap = new HashMap<>();
        for (Information.Type type : requiredInformationTypes) {
            Information information = null;
            try {
                information = user.getInformationByType(type.getId());
            } catch (DibsException.ObjectNotFoundException e) {
                // do nothing
            }
            requiredInformationMap.put(type.getId(), information);
        }

        this.model.put("requiredInformationMap", requiredInformationMap);
        return new Viewable("/application.ftl", this.model);
    }

    /* Application.accept */

    @POST
    @Path("applications/{id}/accept")
    public Response applicationAccept(@PathParam("id") String id) {
        URI url = null;
        Application application = null;
        try {
            application = service.getApplication(id);
            application.accept();
        } catch (IllegalStateException e) {
            // TODO
        }
        url = UriBuilder.fromUri("/applications/{id}/").build(application.getId());
        return Response.seeOther(url).build();
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
            @QueryParam("post") String post, @QueryParam("error") String error) {
        Course course = this.service.getCourse(id);
        this.model.put("course", course);
        this.model.put("applications", course.getApplications());
        this.model.put("ranks", course.getAllocationRule().getQuota().getRanking());
        if (post != null && post.equals("user-connect-to-dosv")) {
            this.model.put("notification",
                "Dein Konto wurde mit hochschulstart.de verbunden. Du kannst dich jetzt auf diesen Studiengang bewerben.");
        }
        if (error != null && error.equals("course_has_applications")) {
            this.model.put("notification",
                "Die Veröffentlichung kann nicht zurückgezogen werden solange es Bewerbungen auf diesen Studiengang gibt.");
        }
        if (error != null && error.equals("course_in_admission")) {
            this.model.put("notification",
                "Das Zulassungsverfahren wurde bereits gestartet. Sie können sich nicht bewerben.");
        }
        if (error != null && error.equals("course_not_published")) {
            this.model.put("notification",
                "Der Studiengang wird noch nicht veröffentlich. Sie können das Zulassungsverfahren nicht starten.");
        }
        return new Viewable("/course.ftl", this.model);
    }

    /* Course.apply */

    @POST
    @Path("courses/{id}/apply")
    public Response courseApply(@PathParam("id") String id) {
        Course course = this.service.getCourse(id);
        URI url = null;

        try {
            Application application = course.apply(this.user.getId(), this.user);
            url = UriBuilder.fromUri("/applications/{id}").build(application.getId());

        } catch (IllegalStateException e) {
            switch (e.getCode()) {
            case "course_not_published":
                // handled by 404 after redirect
                url = UriBuilder.fromUri("/courses/{id}").build(id);
                break;
            case "user_not_connected":
                url = UriBuilder
                    .fromUri("/users/{id}/connect-to-dosv?course-id={course-id}")
                    .build(this.user.getId(), id);
                break;
            case "course_in_admission":
                url = UriBuilder.fromUri("/courses/{id}?error=course_in_admission").build(id);
                break;
            default:
                // unreachable
                throw new RuntimeException(e);
            }
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

    /* Course.startAdmission */

    @POST
    @Path("courses/{id}/start-admission")
    public Response courseStartAdmission(@PathParam("id") String id) {
        // TODO: handle course_unpublished error
        Course course = this.service.getCourse(id);
        URI url = null;

        if (!course.isPublished()) {
            url = UriBuilder.fromUri("/courses/{id}?error=course_not_published").build(id);
        } else {
            course.startAdmission(this.user);
            url = UriBuilder.fromUri("/courses/{id}").build(id);
        }
        return Response.seeOther(url).build();
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
            int capacity;
            try {
                capacity = Integer.parseInt(form.getFirst("capacity"));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("capacity_nan");
            }

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
