package de.huberlin.cms.dosv;

import static de.hochschulstart.hochschulschnittstelle.commonv1_0.ErgebnisStatus.ZURUECKGEWIESEN;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.IN_VORBEREITUNG;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.OEFFENTLICH_SICHTBAR;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
import de.hochschulstart.hochschulschnittstelle.benutzerv1_0.Bewerber;
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
 * Synchronisiert Studiengänge, Bewerbungen und Ranglisten mit dem DoSV. Jedes Datum
 * wird entweder nur im System des DoSV oder lokal geschrieben.
 *
 * @author Markus Michler
 */
public class DosvSync {

    /**
     * Der Benutzer hat sich selbst registriert. Der Benutzer besitzt keinen Zugriff auf
     * wesentliche Systemfunktionalitäten.
     */
    public static final String USER_REGISTERED = "registered";

    /**
     * Der Benutzer hat seine Aktivierungsmail bestätigt, aber das System erfordert eine
     * zusätzliche Autorisierung des Benutzers durch einen Servicestellen-Mitarbeiter. Der
     * Benutzer besitzt keinen Zugriff auf wesentliche Systemfunktionalitäten.
     */
    public static final String USER_AUTHORIZATION_NEEDED = "authorization_needed";

    /**
     * Der Benutzer hat seine Aktivierungsmail bestätigt und eine ggf. erforderliche
     * Autorisierung durch einen Servicestellenmitarbeiter ist erfolgt. Der Benutzer
     * besitzt Zugriff auf alle für ihn erlaubten Systemfunktionalitäten.
     */
    public static final String USER_ACTIVE = "active";

    /**
     * Der Benutzer ist durch einen Mitarbeiter gesperrt worden. Der Benutzer besitzt
     * keinen Zugriff auf wesentliche Systemfunktionalitäten.
     */
    public static final String USER_SUSPENDED = "suspended";

    /**
     * Der Benutzer ist durch einen Mitarbeiter zur Löschung freigegeben worden.
     * Der Benutzer besitzt keinen Zugriff auf wesentliche Systemfunktionalitäten.
     */
    public static final String USER_DELETION_PENDING = "deletion_pending";

    /**
     * Der Benutzer möchte, dass seine Daten aus dem System gelöscht werden.
     * Der Benutzer besitzt keinen Zugriff auf wesentliche Systemfunktionalitäten.
     */
    public static final String USER_DELETION_REQUESTED = "deletion_requested";

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
     * @param dosvBid DoSV Benutzer-ID
     * @param dosvBan DOSV Benutzer-Autorisierungsnummer
     * @return die mit dem Präfix USER gekennzeichneten Konstanten.
     * @throws DosvAuthenticationException wenn BID oder BAN nicht im System des DoSV existieren.
     */
    public String getUserStatus(String dosvBid, String dosvBan) {
        try {
            Bewerber bewerber = dosvClient.abrufenStammdatenDurchHS(dosvBid, dosvBan);
            switch (bewerber.getStatus()) {
            case REGISTRIERT:
                return USER_REGISTERED;
            case AUTORISIERUNG_ERFORDERLICH:
                return USER_AUTHORIZATION_NEEDED;
            case GESPERRT:
                return USER_SUSPENDED;
            case LOESCHBAR:
                return USER_DELETION_PENDING;
            case LOESCHUNG_GEWUENSCHT:
                return USER_DELETION_REQUESTED;
            default: // AKTIV
                return USER_ACTIVE;
            }
        } catch (BenutzerServiceFehler e) {
            if (e.getFaultInfo().getClass() == UnbekannterBenutzerFehler.class) {
                throw new DosvAuthenticationException("bid_not_found",
                    "There is no user registered with this BID.");
            } else if (e.getFaultInfo().getClass() == AutorisierungsFehler.class) {
                throw new DosvAuthenticationException("ban_not_matching",
                    "The transmitted BAN does not match the BID.");
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void synchronize() {
        pushCourses();
    }

    private void pushCourses() {
        List<Studienangebot> studiangebote = new ArrayList<>();
        List<Studienpaket> studienpakete = new ArrayList<>();
        for (Course course : service.getCourses()) {
            if (course.isDosvPushed()) {
                continue;
            }
            StudienangebotsStatus studienangebotsStatus;
            if (!course.isPublished()) {
                studienangebotsStatus = IN_VORBEREITUNG;
            } else {
                studienangebotsStatus = OEFFENTLICH_SICHTBAR;
            }

            /** Studienangebot - SAF 101 */
            Studienfach studienfach = new Studienfach();
            studienfach.setSchluessel(course.getDosvSubjectKey());
            studienfach.setNameDe(course.getName()); //TODO Feld Course.subject
            Abschluss abschluss = new Abschluss();
            abschluss.setSchluessel(course.getDosvDegreeKey());
            abschluss.setNameDe(course.getDosvDegreeKey()); //TODO Feld Course.degree

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

            Koordinierungsangebotsdaten koordinierungsangebotsdaten
                = new Koordinierungsangebotsdaten();
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(new Date()); //TODO Beginn Bewerbungsfrist in Course
            XMLGregorianCalendar xmlCal;
            Duration duration;
            try {
                xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
                duration = DatatypeFactory.newInstance().newDurationYearMonth(true, 1, 0);
            } catch (DatatypeConfigurationException e) {
                // unerreichbar
                throw new RuntimeException(e);
            }
            koordinierungsangebotsdaten.setAnfangBewerbungsfrist((XMLGregorianCalendar) xmlCal.clone());
            xmlCal.add(duration); //TODO Ende Bewerbungsfrist in Course
            koordinierungsangebotsdaten.setEndeBewerbungsfrist(xmlCal);

            koordinierungsangebotsdaten.setUrlHSBewerbungsportal("http://studienplatz.hu-berlin.de/");

            Einfachstudienangebot einfachstudienangebot = new Einfachstudienangebot();
            einfachstudienangebot.setNameDe(course.getName());
            einfachstudienangebot.setBeschreibungDe(course.getName()); //TODO Feld Course.description
            einfachstudienangebot.setStudiengang(studiengang);
            einfachstudienangebot.setIntegrationseinstellungen(integrationseinstellungen);
            einfachstudienangebot.setKoordinierungsangebotsdaten(koordinierungsangebotsdaten);
            einfachstudienangebot.setStatus(studienangebotsStatus);

            studiangebote.add(einfachstudienangebot);

            /** Nur öffentlich sichtbare Studienangebote dürfen mit einem Paket verknüpft werden */
            if (!course.isPublished()) {
                continue;
            }

            /** Studienpaket - SAF 401 */
            EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                new EinfachstudienangebotsSchluessel();
            einfachstudienangebotsSchluessel.setStudienfachSchluessel(course
                .getDosvSubjectKey());
            einfachstudienangebotsSchluessel.setAbschlussSchluessel(course
                .getDosvDegreeKey());

            Bewerberplatzbedarf bewerberplatzbedarf = new Bewerberplatzbedarf();
            bewerberplatzbedarf.setNenner(1);
            bewerberplatzbedarf.setZaehler(1);

            Paketbestandteil paketbestandteil = new Paketbestandteil();
            paketbestandteil
                .setEinfachstudienangebotsSchluessel(einfachstudienangebotsSchluessel);
            paketbestandteil.setBewerberplatzbedarf(bewerberplatzbedarf);

            Studienpaket studienpaket = new Studienpaket();
            studienpaket.setSchluessel(course.getDosvSubjectKey() + "--"
                + course.getDosvDegreeKey());
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
        try {
            String sql = "UPDATE course SET dosv_pushed = TRUE";
            PreparedStatement statement = service.getDb().prepareStatement(sql);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }
}
