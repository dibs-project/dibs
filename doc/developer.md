HUB-Entwicklerdokumentation
===========================

Entwicklungsumgebung einrichten
-------------------------------

Um die Testumgebung zu konfigurieren, kopiere die Datei `test.default.properties` nach
`test.properties` und passe sie deinen Bedürfnissen an.

Zum Testen benötigt HUB eine PostgreSQL-Datenbank. Standardmäßig wird angenommen, dass
eine lokale Datenbank mit dem Namen "hub_test" existiert. In `README.md` ist beschrieben,
wie bei Bedarf eine lokale Datenbank erstellt werden kann.

Für das Testen der Anbindung an das DoSV existieren Integration Tests.
Für jene muss ein Benutzerkonto auf der Testumgebung bei Hochschulstart angelegt und die 
BID und BAN dieses Kontos in `test.properties` eingetragen werden.

HUB testen
----------

Führe einfach folgenden Befehl aus um alle Testfälle durch zu spielen:

    mvn test

Um nur die Integration Tests auszuführen nutze den Befehl

    mvn test-compile failsafe:integration-test

HUB-Entwicklungsserver ausführen
--------------------------------

Starte den HUB-Entwicklungsserver mit:

    ./hub.sh
