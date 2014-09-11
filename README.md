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

Hergestellt mit
---------------

 * Java (7.0) by Oracle - https://www.java.com/
 * PostgreSQL (9.2) by The PostgreSQL Global Development Group -
   http://www.postgresql.org/
 * PostgreSQL JDBC Driver (9.2) by The PostgreSQL Global Development Group -
   http://jdbc.postgresql.org/
 * Apache Commons Lang (3.3) by The Apache Software Foundation -
   https://commons.apache.org/proper/commons-lang/
 * Apache Commons Collections (4.0) by The Apache Software Foundation -
   https://commons.apache.org/proper/commons-collections/
 * Jersey (2.11) by Oracle - https://jersey.java.net/
 * Jetty (9.2) by Mort Bay Consulting Pty Ltd. - https://www.eclipse.org/jetty/
 * JUnit (4.11) by JUnit - http://junit.org/
 * Apache Maven (3.0) by The Apache Software Foundation - https://maven.apache.org/
