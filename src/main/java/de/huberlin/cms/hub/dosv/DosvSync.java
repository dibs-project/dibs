/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.EINGEGANGEN;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.GUELTIG;
import static de.hochschulstart.hochschulschnittstelle.commonv1_0.ErgebnisStatus.ZURUECKGEWIESEN;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.IN_VORBEREITUNG;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.OEFFENTLICH_SICHTBAR;

import java.io.IOError;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import de.hochschulstart.hochschulschnittstelle.benutzerservicev1_0.BenutzerServiceFehler;
import de.hochschulstart.hochschulschnittstelle.bewerbungenserviceparamv1_0.BewerbungErgebnis;
import de.hochschulstart.hochschulschnittstelle.bewerbungenservicev1_0.BewerbungenServiceFehler;
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.Bewerbung;
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus;
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsSchluessel;
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.Einfachstudienangebotsbewerbung;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.AutorisierungsFehler;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.UnbekannterBenutzerFehler;
import de.hochschulstart.hochschulschnittstelle.studiengaengeserviceparamv1_0.StudienangebotErgebnis;
import de.hochschulstart.hochschulschnittstelle.studiengaengeservicev1_0.StudiengaengeServiceFehler;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Abschluss;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.BescheidVersandart;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Bewerbungsort;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Einfachstudienangebot;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.EinfachstudienangebotsSchluessel;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Integrationseinstellungen;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Koordinierungsangebotsdaten;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Studienangebot;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Studienfach;
import de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.Studiengang;
import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.Application;
import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.Settings;
import de.huberlin.cms.hub.User;

/**
 * Synchronisiationsklasse für Studiengänge, Bewerbungen und Ranglisten mit dem DoSV.
 *
 * @author Markus Michler
 */
public class DosvSync {
    private ApplicationService service;
    private Properties dosvConfig;

    public DosvSync(ApplicationService service) {
        this.service = service;
        dosvConfig = new Properties();
        dosvConfig.putAll(service.getConfig());
        Settings settings = service.getSettings();
        dosvConfig.setProperty(DosvClient.SEMESTER, settings.getSemester().substring(4, 6));
        dosvConfig.setProperty(DosvClient.YEAR, settings.getSemester().substring(0, 4));
    }

    /**
     * @param dosvBid DoSV-Benutzer-ID
     * @param dosvBan DoSV-Benutzer-Autorisierungsnummer
     * @return <code>true</code>, wenn der Benutzer authentifiziert wurde,
     * ansonsten <code>false</code>.
     */
    public boolean authenticate(String dosvBid, String dosvBan) {
        try {
            // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
            new DosvClient(dosvConfig).abrufenStammdatenDurchHS(dosvBid, dosvBan);
            return true;
        } catch (BenutzerServiceFehler e) {
            if (e.getFaultInfo() instanceof UnbekannterBenutzerFehler
                    || e.getFaultInfo() instanceof AutorisierungsFehler) {
                return false;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Synchronisiert Studiengänge, Bewerbungen, und Ranglisten mit dem System des DoSV.
     */
    public void synchronize() {
        Date newSyncTime = new Date();
        pushCourses();
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            PreparedStatement statement =
                db.prepareStatement("UPDATE settings SET dosv_sync_time = ?");
            statement.setTimestamp(1, new Timestamp(newSyncTime.getTime()));
            statement.executeUpdate();
            service.getJournal().record(
                ApplicationService.ACTION_TYPE_DOSV_SYNC_SYNCHRONIZED, null, null, null);
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private void pushCourses() {
        List<Studienangebot> studiangebote = new ArrayList<>();

        Date dosvSynctime = service.getSettings().getDosvSyncTime();
        for (Course course : service.getCourses()) {
            if (dosvSynctime.after(course.getModificationTime())) {
                continue;
            }
            // TODO Studienangebote können nur im Status IN_VORBEREITUNG geändert werden,
            // deshalb Änderung und Sichtbarmachung auf zwei übertragene Objekte aufteilen.
            StudienangebotsStatus studienangebotsStatus;
            if (!course.isPublished()) {
                studienangebotsStatus = IN_VORBEREITUNG;
            } else {
                studienangebotsStatus = OEFFENTLICH_SICHTBAR;
            }

            String dosvSubjectKey = Integer.toString(course.getName().hashCode());
            // TODO Studienangebot nicht übertragen, wenn die Zulassung begonnen hat.
            /** Studienangebot - SAF 101 */
            Studienfach studienfach = new Studienfach();
            studienfach.setSchluessel(dosvSubjectKey);
            studienfach.setNameDe(course.getName()); // TODO Feld Course.subject
            Abschluss abschluss = new Abschluss();
            abschluss.setSchluessel("bachelor");
            abschluss.setNameDe("bachelor"); // TODO Feld Course.degree

            Studiengang studiengang = new Studiengang();
            studiengang.setNameDe(course.getName());
            studiengang.setIstNCStudiengang(true);
            studiengang.setStudienfach(studienfach);
            studiengang.setAbschluss(abschluss);

            Integrationseinstellungen integrationseinstellungen =
                new Integrationseinstellungen();
            integrationseinstellungen.setBewerbungsort(Bewerbungsort.HOCHSCHULE);
            integrationseinstellungen.setHzbPruefungGewuenscht(false);
            integrationseinstellungen
                .setZulassungsBescheidVersandart(BescheidVersandart.HOCHSCHULE);
            integrationseinstellungen
                .setRueckstellungsBescheidVersandart(BescheidVersandart.HOCHSCHULE);

            Koordinierungsangebotsdaten koordinierungsangebotsdaten =
                new Koordinierungsangebotsdaten();
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date()); // TODO Beginn Bewerbungsfrist in Course
            XMLGregorianCalendar xmlCal;
            Duration duration;
            try {
                xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                duration = DatatypeFactory.newInstance().newDurationYearMonth(true, 1, 0);
            } catch (DatatypeConfigurationException e) {
                // unerreichbar
                throw new RuntimeException(e);
            }
            koordinierungsangebotsdaten
                .setAnfangBewerbungsfrist((XMLGregorianCalendar) xmlCal.clone());
            xmlCal.add(duration); // TODO Ende Bewerbungsfrist in Course
            koordinierungsangebotsdaten.setEndeBewerbungsfrist(xmlCal);
            koordinierungsangebotsdaten
                .setUrlHSBewerbungsportal("http://studienplatz.hu-berlin.de/");

            Einfachstudienangebot einfachstudienangebot = new Einfachstudienangebot();
            einfachstudienangebot.setNameDe(course.getName());
            // TODO Feld Course.description
            einfachstudienangebot.setBeschreibungDe(course.getName());
            einfachstudienangebot.setStudiengang(studiengang);
            einfachstudienangebot.setIntegrationseinstellungen(integrationseinstellungen);
            einfachstudienangebot
                .setKoordinierungsangebotsdaten(koordinierungsangebotsdaten);
            einfachstudienangebot.setStatus(studienangebotsStatus);

            studiangebote.add(einfachstudienangebot);

            // TODO Studienpaket (SAF 401) nur anlegen/ändern, wenn die Zulassung begonnen hat.
        }
        try {
            List<StudienangebotErgebnis> studienangebotErgebnisse =
             // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                new DosvClient(dosvConfig).anlegenAendernStudienangeboteDurchHS(studiangebote);
            for (StudienangebotErgebnis studienangebotErgebnis : studienangebotErgebnisse) {
                if (studienangebotErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    throw new RuntimeException(
                        ((EinfachstudienangebotsSchluessel) studienangebotErgebnis
                            .getStudienangebotsSchlussel()).getStudienfachSchluessel()
                            + " : " + studienangebotErgebnis.getGrundZurueckweisung());
                }
            }
        } catch (StudiengaengeServiceFehler e) {
            throw new RuntimeException(e);
        }
    }

    private boolean pushApplicationStatus() {
        boolean done = true;
        List<Bewerbung> bewerbungenNeu = new ArrayList<>();
        List<Bewerbung> bewerbungenGeaendert = new ArrayList<>();
        List<Application> applications = service.getApplications();
        for (Application application : applications) {
            if (application.isDosvPushed()) {
                continue;
            }
            EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                new EinfachstudienangebotsSchluessel();
            einfachstudienangebotsSchluessel.setStudienfachSchluessel(application
                .getCourse().getDosvSubjectKey());
            einfachstudienangebotsSchluessel.setAbschlussSchluessel(application
                .getCourse().getDosvDegreeKey());

            User user = application.getUser();
            Einfachstudienangebotsbewerbung einfachstudienangebotsbewerbung =
                new Einfachstudienangebotsbewerbung();
            einfachstudienangebotsbewerbung.setBewerberId(user.getDosvBid());
            einfachstudienangebotsbewerbung.setBewerberBAN(user.getDosvBan());
            einfachstudienangebotsbewerbung.setBewerberEmailAdresse(user.getEmail());
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            try {
                einfachstudienangebotsbewerbung.setEingangsZeitpunkt(DatatypeFactory
                    .newInstance().newXMLGregorianCalendar(cal));
            } catch (DatatypeConfigurationException e) {
                // unerreichbar
                throw new RuntimeException(e);
            }
            einfachstudienangebotsbewerbung
                .setEinfachstudienangebotsSchluessel(einfachstudienangebotsSchluessel);

            BewerbungsBearbeitungsstatus bewerbungsBearbeitungsstatus = null;
            if (application.getDosvVersion() == 0) {
                bewerbungsBearbeitungsstatus = EINGEGANGEN;
                bewerbungenNeu.add(einfachstudienangebotsbewerbung);
            } else {
                bewerbungsBearbeitungsstatus = GUELTIG;
                bewerbungenGeaendert.add(einfachstudienangebotsbewerbung);
            }
            einfachstudienangebotsbewerbung
                .setBearbeitungsstatus(bewerbungsBearbeitungsstatus);
        }

        List<BewerbungErgebnis> zurueckgewiesen = new ArrayList<>();
        try {
            /** SAF 301 */
      // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
            for (BewerbungErgebnis bewerbungErgebnis : new DosvClient(dosvConfig)
                .uebermittelnNeueBewerbungenAnSeSt(bewerbungenNeu)) {
                if (bewerbungErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    BewerbungsSchluessel bewerbungsSchluessel =
                        bewerbungErgebnis.getBewerbungsSchluessel();
                    throw new RuntimeException(bewerbungsSchluessel.getBewerberId()
                        + ", " + bewerbungsSchluessel.getAbschlussSchluessel() + ": "
                        + bewerbungErgebnis.getGrundZurueckweisung());
                }
            }

            /** SAF 302 */
      // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
            for (BewerbungErgebnis bewerbungErgebnis : new DosvClient(dosvConfig)
                .uebermittelnGeaenderteBewerbungenAnSeSt(bewerbungenGeaendert)) {
                if (bewerbungErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    /** Account zur Löschung vorgesehen */
                    if (bewerbungErgebnis.getGrundZurueckweisung().contains("30235")) {
                        zurueckgewiesen.add(bewerbungErgebnis);
                        // Wie gehen wir mit solchen Bewerbungen und den Nutzern um?
                    }
                    /** Versionskonflikt */
                    if (bewerbungErgebnis.getGrundZurueckweisung().contains("30233")) {
                        done = false;
                        zurueckgewiesen.add(bewerbungErgebnis);
                    } else {
                        BewerbungsSchluessel bewerbungsSchluessel =
                            bewerbungErgebnis.getBewerbungsSchluessel();
                        throw new RuntimeException(bewerbungsSchluessel.getBewerberId()
                            + ", " + bewerbungsSchluessel.getAbschlussSchluessel() + ": "
                            + bewerbungErgebnis.getGrundZurueckweisung());
                    }
                }
            }
        } catch (BewerbungenServiceFehler e) {
            throw new RuntimeException(e);
        }

        for (Application application : applications) {
            boolean pushed = true;
            for (BewerbungErgebnis bewerbungErgebnis : zurueckgewiesen) {
                if (bewerbungErgebnis.getBewerbungsSchluessel().getBewerberId()
                    .equals(application.getUser().getDosvBid())) {
                    pushed = false;
                }
            }
            try {
                if (!pushed) {
                    continue;
                }
                String sql = "UPDATE application SET dosv_pushed = TRUE, "
                        + "dosv_version = dosv_version + 1 WHERE id = ?";
                PreparedStatement statement = service.getDb().prepareStatement(sql);
                statement.setString(1, application.getId());
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new IOError(e);
            }
        }
        return done;
    }
}
