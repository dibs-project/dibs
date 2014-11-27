/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.EINGEGANGEN;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.GUELTIG;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZUGELASSEN;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZULASSUNGSANGEBOT_LIEGT_VOR;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZURUECKGEZOGEN;
import static de.hochschulstart.hochschulschnittstelle.commonv1_0.ErgebnisStatus.ZURUECKGEWIESEN;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.IN_VORBEREITUNG;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.OEFFENTLICH_SICHTBAR;
import static de.huberlin.cms.hub.Application.STATUS_ADMITTED;
import static de.huberlin.cms.hub.Application.STATUS_COMPLETE;
import static de.huberlin.cms.hub.Application.STATUS_CONFIRMED;
import static de.huberlin.cms.hub.Application.STATUS_INCOMPLETE;
import static de.huberlin.cms.hub.Application.STATUS_VALID;
import static de.huberlin.cms.hub.Application.STATUS_WITHDRAWN;

import java.io.IOError;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import de.hochschulstart.hochschulschnittstelle.benutzerservicev1_0.BenutzerServiceFehler;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlserviceparamv1_0.RanglisteErgebnis;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlserviceparamv1_0.StudienpaketErgebnis;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlservicev1_0.BewerberauswahlServiceFehler;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Bewerberplatzbedarf;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Paketbestandteil;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Rangliste;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.RanglistenEbene;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.RanglistenStatus;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Ranglisteneintrag;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.RanglisteneintragsStatus;
import de.hochschulstart.hochschulschnittstelle.bewerberauswahlv1_0.Studienpaket;
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
import de.huberlin.cms.hub.Quota;
import de.huberlin.cms.hub.Rank;
import de.huberlin.cms.hub.Settings;
import de.huberlin.cms.hub.User;

// TODO document error handling
/**
 * DoSV synchronisation class for Courses, Applications and Ranks. All resources are synced
 * as early as possible.
 * <p>
 * <strong>Data Mapping between HUB and the DoSV system</strong>
 * <p>
 * General:
 * <ul>
 * <li><code>abschluss.schluessel</code> is always <code>"bachelor"</code></li>
 * <li><code>studienfach.schluessel = course.getId()</code></li>
 * </ul>
 * <p>
 * Courses:
 * <ul>
 * <li><code>published == true -> OEFFENTLICH_SICHTBAR</code></li>
 * <li><code>published == false -> IN_VORBEREITUNG</code></li>
 * <li><code>integrationseinstellungen.bewerbungsort: hochschule</code></li>
 * <li><code>integrationseinstellungen.*bescheidVersandart: hochschule</code></li>
 * <li><code>studienfach.nameDE, einfachstudienangebot.nameDE, *.beschreibungDE =
 * course.name</code></li>
 * <li><code>studienpaket.schluessel = course.getId()</code></li>
 * </ul>
 * <p>
 * Applications:
 * <ul>
 * <li><code>STATUS_INCOMPLETE -> EINGEGANGEN</code></li>
 * <li><code>STATUS_COMPLETE -> EINGEGANGEN</code></li>
 * <li><code>STATUS_VALID -> GUELTIG</code></li>
 * <li><code>ZULASSUNGSANGEBOT_LIEGT_VOR -> STATUS_ADMITTED</code></li>
 * <li><code>ZUGELASSEN -> STATUS_CONFIRMED</code></li>
 * <li><code>ZURUECKGEZOGEN -> STATUS_WITHDRAWN</code></li>
 * </ul>
 * Each application status is set either by HUB or via Hochschulstart.
 * To avoid synchronisation conflicts between <code>STATUS_CONFIRMED</code> and
 * <code>STATUS_WITHDRAWN</code>, users can withdraw their application only via Hochschulstart.
 *
 * Rankings:
 * <ul>
 * <li><code>rangliste.schluessel = quota.getId()</code></li>
 * </ul>
 *
 * @author Markus Michler
 */
public class DosvSync {
    private ApplicationService service;
    private Properties dosvConfig;
    private final static Map<String, BewerbungsBearbeitungsstatus> APPLICATION_DOSV_STATUS;
    private final static Map<BewerbungsBearbeitungsstatus, String> DOSV_APPLICATION_STATUS;

    static {
        APPLICATION_DOSV_STATUS = new HashMap<>();
        APPLICATION_DOSV_STATUS.put(STATUS_INCOMPLETE, EINGEGANGEN);
        APPLICATION_DOSV_STATUS.put(STATUS_COMPLETE, EINGEGANGEN);
        APPLICATION_DOSV_STATUS.put(STATUS_VALID, GUELTIG);

        DOSV_APPLICATION_STATUS = new HashMap<>();
        DOSV_APPLICATION_STATUS.put(ZULASSUNGSANGEBOT_LIEGT_VOR, STATUS_ADMITTED);
        DOSV_APPLICATION_STATUS.put(ZUGELASSEN, STATUS_CONFIRMED);
        DOSV_APPLICATION_STATUS.put(ZURUECKGEZOGEN, STATUS_WITHDRAWN);
    }

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
     * Synchronises Courses, Applications and Ranks.
     */
    public void synchronize() {
        Date newSyncTime = new Date();
        pushCourses();
        boolean applicationsPushed = false;
        int loopCount = 0;
        while (!applicationsPushed) {
            pullApplicationStatus();
            applicationsPushed = pushApplicationStatus();
            loopCount++;
            if (loopCount > 10) {
                throw new RuntimeException("Sync exceeded maximum number of retries.");
            }
        }
        pushRankings();
        try {
            Connection db = service.getDb();
            db.setAutoCommit(false);
            service.getQueryRunner().update(db, "UPDATE settings SET dosv_sync_time = ?",
                new Timestamp(newSyncTime.getTime()));
            service.getJournal().record(
                ApplicationService.ACTION_TYPE_DOSV_SYNC_SYNCHRONIZED, null, null, null);
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private void pushCourses() {
        List<Studienangebot> studienangebote = new ArrayList<>();
        List<Studienpaket> studienpakete = new ArrayList<>();

        Date dosvSyncTime = service.getSettings().getDosvSyncTime();
        for (Course course : service.getCourses()) {
            if (!course.isDosv() || dosvSyncTime.after(course.getModificationTime())) {
                continue;
            }
            // TODO Feld Course.subject
            String dosvCourseKey = course.getId();
            if (course.isAdmission()) {
                /** Studienpaket - SAF 401 */
                EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                    new EinfachstudienangebotsSchluessel();
                einfachstudienangebotsSchluessel.setStudienfachSchluessel(dosvCourseKey);
                einfachstudienangebotsSchluessel.setAbschlussSchluessel("bachelor");

                Bewerberplatzbedarf bewerberplatzbedarf = new Bewerberplatzbedarf();
                bewerberplatzbedarf.setNenner(1);
                bewerberplatzbedarf.setZaehler(1);

                Paketbestandteil paketbestandteil = new Paketbestandteil();
                paketbestandteil
                    .setEinfachstudienangebotsSchluessel(einfachstudienangebotsSchluessel);
                paketbestandteil.setBewerberplatzbedarf(bewerberplatzbedarf);

                Studienpaket studienpaket = new Studienpaket();
                studienpaket.setSchluessel(dosvCourseKey);
                studienpaket.setKapazitaet(course.getCapacity());
                studienpaket.getPaketbestandteil().add(paketbestandteil);
                studienpaket.setNameDe(course.getName());

                studienpakete.add(studienpaket);
                continue;
            }

            // TODO Studienangebote können nur im Status IN_VORBEREITUNG geändert werden,
            // deshalb Änderung und Sichtbarmachung auf zwei übertragene Objekte aufteilen.
            StudienangebotsStatus studienangebotsStatus =
                course.isPublished() ? OEFFENTLICH_SICHTBAR : IN_VORBEREITUNG;

            /** Studienangebot - SAF 101 */
            Studienfach studienfach = new Studienfach();
            studienfach.setSchluessel(dosvCourseKey);
            studienfach.setNameDe(course.getName());
            Abschluss abschluss = new Abschluss();
            abschluss.setSchluessel("bachelor");
            abschluss.setNameDe("Bachelor"); // TODO Feld Course.degree

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
                duration = DatatypeFactory.newInstance().newDuration(1000);
            } catch (DatatypeConfigurationException e) {
                // unerreichbar
                throw new RuntimeException(e);
            }
            koordinierungsangebotsdaten
                .setAnfangBewerbungsfrist((XMLGregorianCalendar) xmlCal.clone());
            xmlCal.add(duration); // TODO Ende Bewerbungsfrist in Course
            koordinierungsangebotsdaten.setEndeBewerbungsfrist(xmlCal);
            koordinierungsangebotsdaten
                .setUrlHSBewerbungsportal("http://example.org/"); // TODO Konfigurierbar

            Einfachstudienangebot einfachstudienangebot = new Einfachstudienangebot();
            einfachstudienangebot.setNameDe(course.getName());
            // TODO Feld Course.description
            einfachstudienangebot.setBeschreibungDe(course.getName());
            einfachstudienangebot.setStudiengang(studiengang);
            einfachstudienangebot.setIntegrationseinstellungen(integrationseinstellungen);
            einfachstudienangebot
                .setKoordinierungsangebotsdaten(koordinierungsangebotsdaten);
            einfachstudienangebot.setStatus(studienangebotsStatus);

            studienangebote.add(einfachstudienangebot);
        }
        try {
            List<StudienangebotErgebnis> studienangebotErgebnisse =
                // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                new DosvClient(dosvConfig).anlegenAendernStudienangeboteDurchHS(studienangebote);
            for (StudienangebotErgebnis studienangebotErgebnis : studienangebotErgebnisse) {
                if (studienangebotErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    throw new RuntimeException(
                        ((EinfachstudienangebotsSchluessel) studienangebotErgebnis
                            .getStudienangebotsSchlussel()).getStudienfachSchluessel()
                            + " : " + studienangebotErgebnis.getGrundZurueckweisung());
                }
            }
            List<StudienpaketErgebnis> studienpaketErgebnisse =
                // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                new DosvClient(dosvConfig).anlegenAendernStudienpaketeDurchHS(studienpakete);
            for (StudienpaketErgebnis studienpaketErgebnis : studienpaketErgebnisse) {
                if (studienpaketErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    throw new RuntimeException(
                        studienpaketErgebnis.getStudienpaketSchluessel() + " : "
                            + studienpaketErgebnis.getGrundZurueckweisung());
                }
            }
        } catch (StudiengaengeServiceFehler | BewerberauswahlServiceFehler e) {
            throw new RuntimeException(e);
        }
    }

    private void pullApplicationStatus() {
        Connection db = service.getDb();
        Date[] updateTime = new Date[1];
        updateTime[0] = service.getSettings().getDosvApplicationsServerTime();
        List<Bewerbung> bewerbungen;

        // hole die geänderten Bewerbungen anhand der updateTime von Hochschulstart
        try {
            List<String> referenzen =
                // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                new DosvClient(dosvConfig)
                    .anfragenNeueGeaenderteBewerbungenDurchHS(updateTime);
            bewerbungen =
                // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                new DosvClient(dosvConfig)
                    .uebermittelnNeueGeaenderteBewerbungenAnHS(referenzen);
        } catch (BewerbungenServiceFehler e) {
            throw new RuntimeException(e);
        }

        // write changed Applications to DB
        try {
            db.setAutoCommit(false);
            for (Bewerbung bewerbung : bewerbungen) {
                String newStatus =
                    DOSV_APPLICATION_STATUS.get(bewerbung.getBearbeitungsstatus());
                Einfachstudienangebotsbewerbung einfachstudienangebotsbewerbung =
                    (Einfachstudienangebotsbewerbung) bewerbung;
                EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                    einfachstudienangebotsbewerbung.getEinfachstudienangebotsSchluessel();
                if (newStatus == null || APPLICATION_DOSV_STATUS.containsKey(newStatus)) {
                    service.getQueryRunner().update(service.getDb(),
                        "UPDATE application SET dosv_version = ? FROM \"user\" WHERE dosv_bid = ? AND course_id = ? AND \"user\".id = user_id",
                        einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsbewerbung.getBewerberId(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel());
                } else {
                    service.getQueryRunner().update(service.getDb(),
                        "UPDATE application SET status = ?, dosv_version = ?, modification_time = CURRENT_TIMESTAMP FROM \"user\" WHERE dosv_bid = ? AND course_id = ? AND \"user\".id = user_id",
                        newStatus, einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsbewerbung.getBewerberId(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel());
                }
            }
            // schreibe den neuen Updatezeitpunkt in die DB
            service.getQueryRunner().update(service.getDb(),
                "UPDATE settings SET dosv_applications_server_time = ?",
                new Timestamp(updateTime[0].getTime()));
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private boolean pushApplicationStatus() {
        boolean done = true;
        List<Bewerbung> bewerbungenNeu = new ArrayList<>();
        List<Bewerbung> bewerbungenGeaendert = new ArrayList<>();
        Date dosvSynctime = service.getSettings().getDosvSyncTime();

        // TODO sollte durch Filterung durch WHERE optimiert werden
        List<Application> applications = service.getApplications();
        for (Application application : applications) {
            BewerbungsBearbeitungsstatus dosvNewStatus =
                APPLICATION_DOSV_STATUS.get(application.getStatus());
            if (dosvSynctime.after(application.getModificationTime())
                    || dosvNewStatus == null || !application.getCourse().isDosv()) {
                continue;
            }
            EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                new EinfachstudienangebotsSchluessel();
            // TODO Feld Course.subject
            einfachstudienangebotsSchluessel.setStudienfachSchluessel(application
                .getCourse().getId());
            // TODO Feld Course.degree
            einfachstudienangebotsSchluessel.setAbschlussSchluessel("bachelor");

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

            int dosvVersion = application.getDosvVersion();
            if (dosvVersion == -1) {
                bewerbungenNeu.add(einfachstudienangebotsbewerbung);
            } else {
                einfachstudienangebotsbewerbung.setVersionSeSt(dosvVersion);
                bewerbungenGeaendert.add(einfachstudienangebotsbewerbung);
            }
            einfachstudienangebotsbewerbung.setBearbeitungsstatus(dosvNewStatus);
        }

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
                        // TODO Fehlerbehandlung, Benachrichtigung des Benutzers
                    }
                    /** Versionskonflikt */
                    if (bewerbungErgebnis.getGrundZurueckweisung().contains("30233")) {
                        done = false;
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

        return done;
    }

    private void pushRankings() {
          for (Course course : service.getCourses()) {
              if (!course.isDosv()
                  || service.getSettings().getDosvSyncTime()
                      .after(course.getModificationTime()) || !course.isAdmission()) {
                  continue;
              }
              Quota quota = course.getAllocationRule().getQuota();
              List<Rank> ranking = quota.getRanking();
              Rangliste rangliste = new Rangliste();

              rangliste.setNameDe(quota.getName());
              rangliste.setSchluessel(quota.getId());
              rangliste.setStudienpaketSchluessel(course.getId());
              rangliste.setStatus(RanglistenStatus.BEFUELLT);
              // Kopfdaten
              rangliste.setPlaetzeProzentual((double) quota.getPercentage());
              // TODO List<Integer, Integer> Quota.level
              rangliste.setEbene(RanglistenEbene.HAUPTQUOTEN);
              rangliste.setIstVorwegzulasserrangliste(false);
              rangliste.setIstChancenrangliste(false);
              rangliste.setAbarbeitungsposition(1);

              for (Rank rank : ranking) {
                  Ranglisteneintrag ranglisteneintrag = new Ranglisteneintrag();
                  ranglisteneintrag.setBewerberId(rank.getUser().getDosvBid());
                  ranglisteneintrag.setRang(rank.getIndex() + 1);
                  ranglisteneintrag.setStatus(RanglisteneintragsStatus.BELEGT);
                  rangliste.getEintrag().add(ranglisteneintrag);
              }

              RanglisteErgebnis ranglisteErgebnis;
              try {
                  // NOTE Instanziierung ist ressourcenintensiv, deshalb hier und nicht im Konstruktor
                  ranglisteErgebnis =
                      new DosvClient(dosvConfig).uebermittelnRanglistenAnSeSt(Arrays
                          .asList(rangliste)).get(0);
              } catch (BewerberauswahlServiceFehler e) {
                  throw new RuntimeException(e);
              }
              if (ranglisteErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                  throw new RuntimeException(ranglisteErgebnis.getGrundZurueckweisung());
              }
          }
      }
}
