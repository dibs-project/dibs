HUB-Entwicklerdokumentation
===========================

Entwicklungsumgebung einrichten
-------------------------------

Um die Testumgebung zu konfigurieren, kopiere die Datei `test.default.properties` nach
`test.properties` und passe sie deinen Bedürfnissen an.

Zum Testen benötigt HUB eine PostgreSQL-Datenbank. Standardmäßig wird angenommen, dass
eine lokale Datenbank mit dem Namen "hub_test" existiert. In `README.md` ist beschrieben,
wie bei Bedarf eine lokale Datenbank erstellt werden kann.

HUB testen
----------

Führe einfach folgenden Befehl aus um alle Testfälle durch zu spielen:

    mvn test

HUB-Entwicklungsserver ausführen
--------------------------------

Starte den HUB-Entwicklungsserver mit:

    ./hub.sh

Style-Richtlinien
-----------------

Farben:

 * Text / Primär: #333
 * Hintergrund:   #fff

Größen:

 * Zeilenhöhe:             1.50rem
 * Margin / Padding:       1.50rem
 * Enges Margin / Padding: 0.25rem
 * Rahmenradius:           0.25rem
