dibs
====

Application system with DoSV-support.

## Dependencies

 * Java         >= 7.0
 * PostgreSQL   >= 9.2
 * Apache Maven >= 3.0

## Building dibs

For building dibs use:

    mvn package

## Configuring dibs

In order to configure dibs, copy the file `src/main/resources/default.properties` to
`dibs.properties` (in the root directory) and edit them according to your needs.

dibs uses a PostgreSQL database. If needed you can create a local Database (with user), by
using the tool `psql` as PostgreSQL superuser (in most cases `postgres`) with the
following commands:

    CREATE USER dibs WITH PASSWORD 'dibs';
    CREATE DATABASE dibs WITH OWNER dibs;

By default, user name, password and database name are set to "dibs".

Before synchronizing dibs to the "Dialogorientiertes Serviceverfahren", the web services
must be configured: Insert values for keys with the `dosv_` prefix.

## Browser Support

We support the current and the previous version of the popular browsers (i.e. Firefox,
Chrome, Safari, Internet Explorer).

## Created with

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

## Contributors

* Markus Michler - markus.michler@uv.hu-berlin.de
* Sven Pfaller  - sven.pfaller@hu-berlin.de
* Phuong Anh Ha - phuong.anh.ha@hu-berlin.de
* David Koschnick - david.koschnick@staff.hu-berlin.de
* Lars Schilhaneck - lars.schilhaneck.1@hu-berlin.de
* Johannes Caspary - johannes.caspary@cms.hu-berlin.de

Copyright (C) 2015  Humboldt-Universität zu Berlin
