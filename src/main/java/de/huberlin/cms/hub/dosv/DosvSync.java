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
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.Einfachstudienangebotsbewerbung;
import de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.EinfachstudienangebotsbewerbungsSchluessel;
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
 * <li><code>STATUS_ADMITTED <- ZULASSUNGSANGEBOT_LIEGT_VOR</code></li>
 * <li><code>STATUS_CONFIRMED <- ZUGELASSEN</code></li>
 * <li><code>STATUS_WITHDRAWN <- ZURUECKGEZOGEN</code></li>
 * </ul>
 * <p>
 * Each application status is set either by HUB or via Hochschulstart.de.
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
    private final static Map<String, BewerbungsBearbeitungsstatus> APPLICATION_STATUS_MAPPING_TO_DOSV;
    private final static Map<BewerbungsBearbeitungsstatus, String> APPLICATION_STATUS_MAPPING_FROM_DOSV;
    private ApplicationService service;
    private Properties dosvConfig;

    static {
        APPLICATION_STATUS_MAPPING_TO_DOSV = new HashMap<>();
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_INCOMPLETE, EINGEGANGEN);
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_COMPLETE, EINGEGANGEN);
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_VALID, GUELTIG);

        APPLICATION_STATUS_MAPPING_FROM_DOSV = new HashMap<>();
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZULASSUNGSANGEBOT_LIEGT_VOR,
            STATUS_ADMITTED);
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZUGELASSEN, STATUS_CONFIRMED);
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZURUECKGEZOGEN, STATUS_WITHDRAWN);
    }

    private static XMLGregorianCalendar toXMLGregorianCalendar(Date date) {
        XMLGregorianCalendar xmlCal;
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        try {
            xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (DatatypeConfigurationException e) {
            // unreachable
            throw new RuntimeException(e);
        }
        return xmlCal;
    }

    public DosvSync(ApplicationService service) {
        // TODO validate dosvConfig
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
     *
     * @throws
     */
    public void synchronize() {
        Date newSyncTime = new Date();
        pushCourses();
        boolean applicationsPushed = false;
        // TODO adjust number of retries to minimize the possibility of a RuntimeException
        for (int i = 0; !applicationsPushed && i < 10; i++) {
            pullApplicationStatus();
            applicationsPushed = pushApplications();
        }
        if (!applicationsPushed) {
            throw new RuntimeException("Sync exceeded maximum number of retries.");
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
                /* Studienpaket - SAF 401 */
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

            /* Studienangebot - SAF 101 */
            Studienfach studienfach = new Studienfach();
            studienfach.setSchluessel(dosvCourseKey);
            studienfach.setNameDe(course.getName());
            Abschluss abschluss = new Abschluss();
            abschluss.setSchluessel("bachelor");
            abschluss.setNameDe("Bachelor"); // TODO Field Course.degree

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

            // TODO application period in Course
            Date startApplicationTime = new Date();
            Date endApplicationTime = new Date(startApplicationTime.getTime() + 1000);
            koordinierungsangebotsdaten
                .setAnfangBewerbungsfrist(toXMLGregorianCalendar(startApplicationTime));
            koordinierungsangebotsdaten
                .setEndeBewerbungsfrist(toXMLGregorianCalendar(endApplicationTime));
            koordinierungsangebotsdaten
                .setUrlHSBewerbungsportal("http://example.org/"); // TODO configure

            Einfachstudienangebot einfachstudienangebot = new Einfachstudienangebot();
            einfachstudienangebot.setNameDe(course.getName());
            // TODO Field Course.description
            einfachstudienangebot.setBeschreibungDe(course.getName());
            einfachstudienangebot.setStudiengang(studiengang);
            einfachstudienangebot.setIntegrationseinstellungen(integrationseinstellungen);
            einfachstudienangebot
                .setKoordinierungsangebotsdaten(koordinierungsangebotsdaten);
            einfachstudienangebot.setStatus(studienangebotsStatus);

            studienangebote.add(einfachstudienangebot);
        }
        try {
            // NOTE Instantiation is resource intensive so it happens here and not in the constructor
            DosvClient dosvClient = new DosvClient(dosvConfig);

            List<StudienangebotErgebnis> studienangebotErgebnisse =
                dosvClient.anlegenAendernStudienangeboteDurchHS(studienangebote);
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
        } catch (StudiengaengeServiceFehler | BewerberauswahlServiceFehler e) {
            throw new RuntimeException(e);
        }
    }

    private void pullApplicationStatus() {
        Connection db = service.getDb();
        Date[] updateTime = new Date[1];
        updateTime[0] = service.getSettings().getDosvApplicationsServerTime();
        List<Bewerbung> bewerbungen;

        /* SAF 303 */
        // NOTE Instantiation is resource intensive so it happens here and not in the constructor
        DosvClient dosvClient = new DosvClient(dosvConfig);
        try {
            List<String> referenzen =
                dosvClient.anfragenNeueGeaenderteBewerbungenDurchHS(updateTime);
            bewerbungen =
                dosvClient.uebermittelnNeueGeaenderteBewerbungenAnHS(referenzen);
        } catch (BewerbungenServiceFehler e) {
            throw new RuntimeException(e);
        }

        // write changed Applications to DB
        try {
            db.setAutoCommit(false);
            for (Bewerbung bewerbung : bewerbungen) {
                String newStatus =
                    APPLICATION_STATUS_MAPPING_FROM_DOSV.get(bewerbung.getBearbeitungsstatus());
                Einfachstudienangebotsbewerbung einfachstudienangebotsbewerbung =
                    (Einfachstudienangebotsbewerbung) bewerbung;
                EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                    einfachstudienangebotsbewerbung.getEinfachstudienangebotsSchluessel();
                if (newStatus == null) {
                    service.getQueryRunner().update(service.getDb(),
                        "UPDATE application SET dosv_version = ? "
                        + "WHERE course_id = ? AND EXISTS (SELECT id FROM \"user\" WHERE id = user_id AND dosv_bid = ?)",
                        einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel(),
                        einfachstudienangebotsbewerbung.getBewerberId());
                } else {
                    service.getQueryRunner().update(service.getDb(),
                        "UPDATE application SET status = ?, dosv_version = ?, modification_time = CURRENT_TIMESTAMP "
                        + "WHERE course_id = ? AND EXISTS (SELECT id FROM \"user\" WHERE id = user_id AND dosv_bid = ?)",
                        newStatus, einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel(),
                        einfachstudienangebotsbewerbung.getBewerberId());
                }
            }

            service.getQueryRunner().update(service.getDb(),
                "UPDATE settings SET dosv_remote_applications_pull_time = ?",
                new Timestamp(updateTime[0].getTime()));
            db.commit();
            db.setAutoCommit(true);
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private boolean pushApplications() {
        boolean done = true;
        List<Bewerbung> bewerbungenNeu = new ArrayList<>();
        List<Bewerbung> bewerbungenGeaendert = new ArrayList<>();
        Date dosvSynctime = service.getSettings().getDosvSyncTime();

        // TODO should be optimized by WHERE filter
        List<Application> applications = service.getApplications();
        for (Application application : applications) {
            BewerbungsBearbeitungsstatus dosvNewStatus =
                APPLICATION_STATUS_MAPPING_TO_DOSV.get(application.getStatus());
            Course course = application.getCourse();
            if (dosvSynctime.after(application.getModificationTime())
                    || dosvNewStatus == null || !course.isDosv()) {
                continue;
            }
            EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                new EinfachstudienangebotsSchluessel();

            // TODO Field Course.subject
            einfachstudienangebotsSchluessel.setStudienfachSchluessel(course.getId());
            // TODO Field Course.degree
            einfachstudienangebotsSchluessel.setAbschlussSchluessel("bachelor");

            User user = application.getUser();
            Einfachstudienangebotsbewerbung einfachstudienangebotsbewerbung =
                new Einfachstudienangebotsbewerbung();
            einfachstudienangebotsbewerbung.setBewerberId(user.getDosvBid());
            einfachstudienangebotsbewerbung.setBewerberBAN(user.getDosvBan());
            einfachstudienangebotsbewerbung.setBewerberEmailAdresse(user.getEmail());
            einfachstudienangebotsbewerbung
                .setEingangsZeitpunkt(toXMLGregorianCalendar(new Date()));
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
            // NOTE Instantiation is resource intensive so it happens here and not in the constructor
            DosvClient dosvClient = new DosvClient(dosvConfig);

            /* SAF 301 */
            List<BewerbungErgebnis> bewerbungErgebnisse = (dosvClient
                .uebermittelnNeueBewerbungenAnSeSt(bewerbungenNeu));

            /* SAF 302 */
            bewerbungErgebnisse.addAll(dosvClient
                .uebermittelnGeaenderteBewerbungenAnSeSt(bewerbungenGeaendert));

           for (BewerbungErgebnis bewerbungErgebnis : bewerbungErgebnisse) {
               if (bewerbungErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                   /* "Account zur Löschung vorgesehen" */
                   if (bewerbungErgebnis.getGrundZurueckweisung().contains("30235")) {
                       // TODO error handling, user notification
                   }
                   /* "Versionskonflikt" */
                   if (bewerbungErgebnis.getGrundZurueckweisung().contains("30233")) {
                       done = false;
                   } else {
                       // unreachable
                       EinfachstudienangebotsbewerbungsSchluessel bewerbungsSchluessel
                           = (EinfachstudienangebotsbewerbungsSchluessel)
                           bewerbungErgebnis.getBewerbungsSchluessel();
                        throw new RuntimeException(String.format("%, %: %",
                            bewerbungsSchluessel.getBewerberId(),
                            bewerbungsSchluessel.getFachkennzeichenSchluessel(),
                            bewerbungErgebnis.getGrundZurueckweisung()));
                   }
               }
           }
        } catch (BewerbungenServiceFehler e) {
            // unreachable
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
              // "Kopfdaten"
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
                  // NOTE Instantiation is resource intensive so it happens here and not in the constructor
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
