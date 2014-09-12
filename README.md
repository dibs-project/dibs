HUB
===

Bewerbungssystem mit DoSV-Unterstützung.

Dependencies
------------

 * Java         >= 7.0
 * PostgreSQL   >= 9.2
 * Apache Maven >= 3.0

HUB bauen
---------

Führe einfach folgenden Befehl aus um HUB zu bauen:

    mvn compile

HUB einrichten
--------------

HUB benötigt eine PostgreSQL-Datenbank. Bei Bedarf kannst du eine Datenbank (inkl.
Datenbankbenutzer) lokal erstellen, indem du mit dem Werkzeug `psql` als
PostgreSQL-Superuser (oft `postgres`) folgende Befehle ausführst:

    CREATE USER hub WITH PASSWORD 'hub';
    CREATE DATABASE hub WITH OWNER hub;

Hier sind für den Benutzernamen, das Passwort und den Datenbanknamen "hub" gewählt, die
Werte sind aber beliebig.

Browser-Support
---------------

Wir unterstützen jeweils die aktuelle und eine vorhergende Version der bekannten Browser
(Firefox, Chrome, Safari, Internet Explorer).

Hergestellt mit
---------------

 * Java (7.0) von Oracle - https://www.java.com/
 * PostgreSQL (9.2) von The PostgreSQL Global Development Group -
   http://www.postgresql.org/
 * PostgreSQL JDBC Driver (9.2) von The PostgreSQL Global Development Group -
   http://jdbc.postgresql.org/
 * Apache Commons Lang (3.3) von The Apache Software Foundation -
   https://commons.apache.org/proper/commons-lang/
 * Apache Commons Collections (4.0) von The Apache Software Foundation -
   https://commons.apache.org/proper/commons-collections/
 * Java Servlet API (3.1) von Oracle - https://java.net/projects/servlet-spec/
 * Jersey (2.11) von Oracle - https://jersey.java.net/
 * FreeMarker (2.3) von Attila Szegedi, Daniel Dekany, Jonathan Revusky -
   http://freemarker.org/
 * Jetty (9.2) von Mort Bay Consulting Pty Ltd. - https://www.eclipse.org/jetty/
 * Open Sans (1.1) von Google - http://opensans.com/
 * JUnit (4.11) von JUnit - http://junit.org/
 * Apache Maven (3.0) von The Apache Software Foundation - https://maven.apache.org/
