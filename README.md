HUB
===

Bewerbungssystem mit DoSV-Unterstützung.

Dependencies
------------

 * Java >= 1.7
 * PostgreSQL >= 9.1
 * Maven >= 3.0

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
