HUB-Entwicklerdokumentation
===========================

Entwicklungsumgebung einrichten
-------------------------------

Um die Testumgebung zu konfigurieren, kopiere die Datei `test.default.properties` nach
`test.properties` und passe sie deinen Bürfnissen an.

Zum Testen benötigt HUB eine PostgreSQL-Datenbank. Standardmäßig wird angenommen, dass
eine lokale Datenbank mit dem Namen "hub_test" existiert. In `README.md` ist beschrieben,
wie bei Bedarf eine lokale Datenbank erstellt werden kann.

HUB testen
----------

Führe einfach folgenden Befehl aus um alle Testfälle durch zu spielen:

    mvn test
