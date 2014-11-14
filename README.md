HUB
===

Applicationsystem with DoSV-Support.

Dependencies
------------

 * Java         >= 7.0
 * PostgreSQL   >= 9.2
 * Apache Maven >= 3.0

build HUB
---------

for building HUB use:

    mvn compile

configure HUB
-------------

HUB uses a PostgreSQL-Database. If needed you can create a local Database (with User),
by using the tool `psql` as PostgreSQL-Superuser (in most cases `postgres`) with following commands:

    CREATE USER hub WITH PASSWORD 'hub';
    CREATE DATABASE hub WITH OWNER hub;

By default the Username, Password and Databasename is set to "hub".

Before Synchronizing from HUB to "Dialogorientierten Serviceverfahren" 
the Webservices must be configured. Insert Values für keys with dosv_ Prefix.

Created with
------------

 * Java (7.0) from Oracle - https://www.java.com/
 * PostgreSQL (9.2) from The PostgreSQL Global Development Group -
   http://www.postgresql.org/
 * PostgreSQL JDBC Driver (9.2) from The PostgreSQL Global Development Group -
   http://jdbc.postgresql.org/
 * Apache Commons Lang (3.3) from The Apache Software Foundation -
   https://commons.apache.org/proper/commons-lang/
 * Apache Commons Collections (4.0) from The Apache Software Foundation -
   https://commons.apache.org/proper/commons-collections/
 * Apache Commons DbUtils (1.6) von The Apache Software Foundation - 
   http://commons.apache.org/proper/commons-dbutils/
 * Java Servlet API (3.1) from Oracle - https://java.net/projects/servlet-spec/
 * Jersey (2.11) from Oracle - https://jersey.java.net/
 * FreeMarker (2.3) from Attila Szegedi, Daniel Dekany, Jonathan Revusky -
   http://freemarker.org/
 * Jetty (9.2) from Mort Bay Consulting Pty Ltd. - https://www.eclipse.org/jetty/
 * JUnit (4.11) from JUnit - http://junit.org/
 * Apache Maven (3.0) from The Apache Software Foundation - https://maven.apache.org/