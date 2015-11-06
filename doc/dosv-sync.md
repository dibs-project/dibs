# Synchronization with the DoSV Webservices

The DoSV synchronization uploads all applications for courses marked as DoSV and synchronizes their
status. It also uploads the DoSV-courses themselves and their rankings. It is designed to run at a
set interval, `dosv_sync_interval` in the configuration file. The prerequisite for pushing a ranking
to Hochschulstart.de is the corresponding course being in the admission phase.

You cannot apply via Hochschulstart.de to courses that were created with dibs. Users can withdraw
their application only via Hochschulstart.de. Accepting a selection by dibs (DoSV-status
"zugelassen") can also only be done via Hochschulstart.de.

## Status Mapping

Applications

    dibs                    DoSV
    Unvollständig        -> Eingegangen
    Vollständig          -> Eingegangen
    Gültig               -> Gültig
    Zurückgezogen        <- Zurückgezogen
    Zugelassen           <- Zulassungsangebot liegt vor
    Zulassung angenommen <- Zugelassen

Courses

    Veröffentlicht   -> Öffentlich sichtbar
    Nicht öffentlich -> In Vorbereitung
