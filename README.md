HUB
===

Application system with DoSV-support.

Dependencies
------------

 * Java         >= 7.0
 * PostgreSQL   >= 9.2
 * Apache Maven >= 3.0

Building HUB
---------

For building HUB use:

    mvn compile

Configuring HUB
---------------

In order to configure HUB, copy the file `default.properties` to `hub.properties`
and edit them according to your needs.

HUB uses a PostgreSQL database. If needed you can create a local Database (with user),
by using the tool `psql` as PostgreSQL superuser (in most cases `postgres`) with 
the following commands:

    CREATE USER hub WITH PASSWORD 'hub';
    CREATE DATABASE hub WITH OWNER hub;

By default, user name, password and database name are set to "hub".

Before synchronizing HUB to the "Dialogorientiertes Serviceverfahren",
the web services must be configured: Insert values for keys with the `dosv_` 
prefix.

Browser Support
---------------

We support the current and the previous version of the popular browsers (i.e. Firefox,
Chrome, Safari, Internet Explorer)

Created with
------------

 * Java (7.0) by Oracle - https://www.java.com/
 * PostgreSQL (9.2) by The PostgreSQL Global Development Group -
   http://www.postgresql.org/
 * PostgreSQL JDBC Driver (9.2) by The PostgreSQL Global Development Group -
   http://jdbc.postgresql.org/
 * Apache Commons Lang (3.3) by The Apache Software Foundation -
   https://commons.apache.org/proper/commons-lang/
 * Apache Commons Collections (4.0) by The Apache Software Foundation -
   https://commons.apache.org/proper/commons-collections/
 * Apache Commons DbUtils (1.6) by The Apache Software Foundation - 
   http://commons.apache.org/proper/commons-dbutils/
 * Java Servlet API (3.1) by Oracle - https://java.net/projects/servlet-spec/
 * Jersey (2.11) by Oracle - https://jersey.java.net/
 * FreeMarker (2.3) by Attila Szegedi, Daniel Dekany, Jonathan Revusky -
   http://freemarker.org/
 * Jetty (9.2) by Mort Bay Consulting Pty Ltd. - https://www.eclipse.org/jetty/
 * Open Sans (1.1) by Google - http://opensans.com/
 * JUnit (4.11) by JUnit - http://junit.org/
 * Apache Maven (3.0) by The Apache Software Foundation - https://maven.apache.org/