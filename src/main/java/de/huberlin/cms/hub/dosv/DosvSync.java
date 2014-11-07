/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

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
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlserviceparamv1_0.StudienpaketErgebnis;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlservicev1_0.BewerberauswahlServiceFehler;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Bewerberplatzbedarf;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Paketbestandteil;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Studienpaket;
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
import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Course;
import de.huberlin.cms.hub.Settings;

/**
 * Synchronisiationsklasse für Studiengänge, Bewerbungen und Ranglisten mit dem DoSV.
 * Jedes Datum wird ausschließlich entweder im System des DoSV oder in HUB verändert.
 *
 * @author Markus Michler
 */
public class DosvSync {
    private DosvClient dosvClient;
    private ApplicationService service;

    public DosvSync(ApplicationService service) {
        this.service = service;
        Properties dosvConfig = service.getConfig();
        Settings settings = service.getSettings();
        dosvConfig.setProperty(DosvClient.SEMESTER, settings.getSemester().substring(4, 6));
        dosvConfig.setProperty(DosvClient.YEAR, settings.getSemester().substring(0, 4));
        dosvClient = new DosvClient(dosvConfig);
    }

    /**
     * @param dosvBid DoSV-Benutzer-ID
     * @param dosvBan DOSV-Benutzer-Autorisierungsnummer
     * @return <code>true</code>, wenn der Benutzer authentifiziert wurde,
     * ansonsten <code>false</code>.
     */
    public boolean authenticate(String dosvBid, String dosvBan) {
        try {
            dosvClient.abrufenStammdatenDurchHS(dosvBid, dosvBan);
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
        Timestamp newSyncTime = new Timestamp(new Date().getTime());
        pushCourses();
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            PreparedStatement statement =
                db.prepareStatement("UPDATE settings SET dosv_sync_time = ?");
            statement.setTimestamp(1, newSyncTime);
            statement.executeUpdate();
            // FIXME Journal
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private void pushCourses() {
        List<Studienangebot> studiangebote = new ArrayList<>();
        List<Studienpaket> studienpakete = new ArrayList<>();
        Date dosvSynctime = service.getSettings().getDosvSyncTime();
        for (Course course : service.getCourses()) {
            if (dosvSynctime.after(course.getModificationTime())) {
                continue;
            }
            // FIXME Studienangebote nur als IN_VORBEREITUNG übertragen, publizierte dann auf OEFFENTLICH_SICHTBAR setzen
            StudienangebotsStatus studienangebotsStatus;
            if (!course.isPublished()) {
                studienangebotsStatus = IN_VORBEREITUNG;
            } else {
                studienangebotsStatus = OEFFENTLICH_SICHTBAR;
            }

            String dosvSubjectKey = Integer.toString(course.getName().hashCode());
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

            /** Nur öffentlich sichtbare Studienangebote dürfen mit einem Paket verknüpft werden */
            if (!course.isPublished()) {
                continue;
            }

            /** Studienpaket - SAF 401 */
            EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                new EinfachstudienangebotsSchluessel();
            einfachstudienangebotsSchluessel.setStudienfachSchluessel(dosvSubjectKey);
            einfachstudienangebotsSchluessel.setAbschlussSchluessel("bachelor");

            Bewerberplatzbedarf bewerberplatzbedarf = new Bewerberplatzbedarf();
            bewerberplatzbedarf.setNenner(1);
            bewerberplatzbedarf.setZaehler(1);

            Paketbestandteil paketbestandteil = new Paketbestandteil();
            paketbestandteil
                .setEinfachstudienangebotsSchluessel(einfachstudienangebotsSchluessel);
            paketbestandteil.setBewerberplatzbedarf(bewerberplatzbedarf);

            Studienpaket studienpaket = new Studienpaket();
            studienpaket.setSchluessel(dosvSubjectKey + "--" + "bachelor");
            studienpaket.setKapazitaet(course.getCapacity());
            studienpaket.getPaketbestandteil().add(paketbestandteil);
            studienpaket.setNameDe(course.getName());

            studienpakete.add(studienpaket);
        }
        try {
            List<StudienangebotErgebnis> studienangebotErgebnisse =
                dosvClient.anlegenAendernStudienangeboteDurchHS(studiangebote);
            for (StudienangebotErgebnis studienangebotErgebnis : studienangebotErgebnisse) {
                if (studienangebotErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    throw new RuntimeException(
                        ((EinfachstudienangebotsSchluessel) studienangebotErgebnis
                            .getStudienangebotsSchlussel()).getStudienfachSchluessel()
                            + " : " + studienangebotErgebnis.getGrundZurueckweisung());
                }
            }
            List<StudienpaketErgebnis> studienpaketErgebnisse =
                dosvClient.anlegenAendernStudienpaketeDurchHS(studienpakete);
            for (StudienpaketErgebnis studienpaketErgebnis : studienpaketErgebnisse) {
                if (studienpaketErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    throw new RuntimeException(
                        studienpaketErgebnis.getStudienpaketSchluessel() + " : "
                            + studienpaketErgebnis.getGrundZurueckweisung());
                }
            }
        } catch (StudiengaengeServiceFehler e) {
            throw new RuntimeException(e);
        } catch (BewerberauswahlServiceFehler e) {
            throw new RuntimeException(e);
        }
    }
}
