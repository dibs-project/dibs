/*
 * dibs
 * Copyright (C) 2015  Humboldt-Universität zu Berlin
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If
 * not, see <http://www.gnu.org/licenses/>.
 */

package university.dibs.dibs.dosv;

import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.EINGEGANGEN;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.GUELTIG;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZUGELASSEN;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZULASSUNGSANGEBOT_LIEGT_VOR;
import static de.hochschulstart.hochschulschnittstelle.bewerbungenv1_0.BewerbungsBearbeitungsstatus.ZURUECKGEZOGEN;
import static de.hochschulstart.hochschulschnittstelle.commonv1_0.ErgebnisStatus.ZURUECKGEWIESEN;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.IN_VORBEREITUNG;
import static de.hochschulstart.hochschulschnittstelle.studiengaengev1_0.StudienangebotsStatus.OEFFENTLICH_SICHTBAR;
import static university.dibs.dibs.Application.STATUS_ADMITTED;
import static university.dibs.dibs.Application.STATUS_COMPLETE;
import static university.dibs.dibs.Application.STATUS_CONFIRMED;
import static university.dibs.dibs.Application.STATUS_INCOMPLETE;
import static university.dibs.dibs.Application.STATUS_VALID;
import static university.dibs.dibs.Application.STATUS_WITHDRAWN;

import university.dibs.dibs.Application;
import university.dibs.dibs.ApplicationService;
import university.dibs.dibs.Course;
import university.dibs.dibs.Quota;
import university.dibs.dibs.Rank;
import university.dibs.dibs.Settings;
import university.dibs.dibs.User;

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
import org.w3c.dom.Document;

import java.io.IOError;
import java.io.StringWriter;
import java.io.Writer;
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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;

// TODO document error handling
/**
 * DoSV synchronisation class for Courses, Applications and Ranks. All resources are synced as early
 * as possible.
 *
 * <p><strong>Data Mapping between dibs and the DoSV system</strong>
 *
 * <p>General:
 * <ul>
 * <li><code>abschluss.schluessel</code> is always <code>"bachelor"</code></li>
 * <li><code>studienfach.schluessel = course.getId()</code></li>
 * </ul>
 *
 * <p>Courses:
 * <ul>
 * <li><code>published == true -> OEFFENTLICH_SICHTBAR</code></li>
 * <li><code>published == false -> IN_VORBEREITUNG</code></li>
 * <li><code>integrationseinstellungen.bewerbungsort: hochschule</code></li>
 * <li><code>integrationseinstellungen.*bescheidVersandart: hochschule</code></li>
 * <li><code>studienfach.nameDE, einfachstudienangebot.nameDE, *.beschreibungDE = course.name
 * </code></li>
 * <li><code>studienpaket.schluessel = course.getId()</code></li>
 * </ul>
 *
 * <p>Applications:
 * <ul>
 * <li><code>STATUS_INCOMPLETE -> EINGEGANGEN</code></li>
 * <li><code>STATUS_COMPLETE -> EINGEGANGEN</code></li>
 * <li><code>STATUS_VALID -> GUELTIG</code></li>
 * <li><code>STATUS_ADMITTED <- ZULASSUNGSANGEBOT_LIEGT_VOR</code></li>
 * <li><code>STATUS_CONFIRMED <- ZUGELASSEN</code></li>
 * <li><code>STATUS_WITHDRAWN <- ZURUECKGEZOGEN</code></li>
 * </ul>
 *
 * <p>Each application status is set either by dibs or via Hochschulstart.de. To avoid
 * synchronisation conflicts between <code>STATUS_CONFIRMED</code> and
 * <code>STATUS_WITHDRAWN</code>, users can withdraw their application only via Hochschulstart.
 *
 * <p>Rankings:
 * <ul>
 * <li><code>rangliste.schluessel = quota.getId()</code></li>
 * </ul>
 *
 * @author Markus Michler
 */
public class DosvSync {
    private static Logger logger = Logger.getLogger(DosvSync.class.getPackage().getName());

    private static final Map<String, BewerbungsBearbeitungsstatus>
        APPLICATION_STATUS_MAPPING_TO_DOSV;
    private static final Map<BewerbungsBearbeitungsstatus, String>
        APPLICATION_STATUS_MAPPING_FROM_DOSV;
    private static final Map<String, String> NAMESPACES;
    private static final String WS_VERSION = "2";

    private ApplicationService service;
    private Properties dosvConfig;
    private Map<String, Dispatch<SOAPMessage>> dispatches;

    static {
        APPLICATION_STATUS_MAPPING_TO_DOSV = new HashMap<>();
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_INCOMPLETE, EINGEGANGEN);
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_COMPLETE, EINGEGANGEN);
        APPLICATION_STATUS_MAPPING_TO_DOSV.put(STATUS_VALID, GUELTIG);

        APPLICATION_STATUS_MAPPING_FROM_DOSV = new HashMap<>();
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZULASSUNGSANGEBOT_LIEGT_VOR, STATUS_ADMITTED);
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZUGELASSEN, STATUS_CONFIRMED);
        APPLICATION_STATUS_MAPPING_FROM_DOSV.put(ZURUECKGEZOGEN, STATUS_WITHDRAWN);

        NAMESPACES = new HashMap<>();
        NAMESPACES.put("common", "http://CommonV1_0.HochschulSchnittstelle.hochschulstart.de");
        NAMESPACES.put("benutzer", "http://BenutzerServiceParamV1_0.HochschulSchnittstelle.hochschulstart.de");
    }

    /**
     * Initializes DosvSync.
     *
     * @param service <code>ApplicationService</code> used for accessing dibs.
     */
    public DosvSync(ApplicationService service) {
        // TODO validate dosvConfig
        this.service = service;
        this.dosvConfig = new Properties();
        this.dosvConfig.putAll(service.getConfig());
        Settings settings = service.getSettings();
        this.dosvConfig.setProperty(DosvClient.SEMESTER, settings.getSemester().substring(4, 6));
        this.dosvConfig.setProperty(DosvClient.YEAR, settings.getSemester().substring(0, 4));

        // webservice configuration
        // TODO throw ConfigurationException when configuration is missing
        Properties config = service.getConfig();
        List<String> wsEndpointSuffixes = Arrays.asList("studiengaengeService", "benutzerService",
            "bewerbungenService", "bewerberauswahlService");
        this.dispatches = new HashMap<>();
        QName wsName = new QName("dosv");
        Service webService = Service.create(wsName);
        String wsUrl = config.getProperty("dosv_url");
        for (String wsEndpointSuffix : wsEndpointSuffixes) {
            QName portName = new QName(wsEndpointSuffix);
            webService.addPort(portName, SOAPBinding.SOAP11HTTP_BINDING, wsUrl + wsEndpointSuffix);
            Dispatch<SOAPMessage> dispatch =
                webService.createDispatch(portName, SOAPMessage.class, Mode.MESSAGE);
            Map<String, Object> requestContext = dispatch.getRequestContext();
            requestContext.put(BindingProvider.USERNAME_PROPERTY,
                config.getProperty("dosv_user"));
            requestContext.put(BindingProvider.PASSWORD_PROPERTY,
                config.getProperty("dosv_password"));
            this.dispatches.put(wsEndpointSuffix, dispatch);
            if (logger.isLoggable(Level.FINE)) {
                @SuppressWarnings("rawtypes")
                // Typecast to Handler<SOAPMessageContext> not possible
                List<Handler> handlerChain = dispatch.getBinding().getHandlerChain();
                handlerChain.add(new SoapLogger());
                dispatch.getBinding().setHandlerChain(handlerChain);
            }
        }
    }

    /**
     * @param dosvBid DoSV-Benutzer-ID
     * @param dosvBan DoSV-Benutzer-Autorisierungsnummer
     * @return <code>true</code>, if the user has been authenticated, otherwise
     * <code>false</code>.
     */
    public boolean authenticate(String dosvBid, String dosvBan) {
        SOAPMessage request;
        try {
            request = MessageFactory.newInstance().createMessage();
            SOAPBody body = request.getSOAPBody();
            SOAPBodyElement requestElement =
                body.addBodyElement(new QName(NAMESPACES.get("benutzer"),
                    "abrufenStammdatenDurchHSRequest", "benutzer"));
            requestElement.addNamespaceDeclaration(XMLConstants.DEFAULT_NS_PREFIX,
                NAMESPACES.get("common"));
            requestElement.addChildElement("version").setValue(WS_VERSION);
            requestElement.addChildElement("bewerberId", "benutzer").setValue(dosvBid);
            requestElement.addChildElement("BAN", "benutzer").setValue(dosvBan);
        } catch (SOAPException e) {
            // unreachable
            throw new RuntimeException(e);
        }

        try {
            this.dispatches.get("benutzerService").invoke(request);
        } catch (SOAPFaultException e) {
            String faultType = e.getFault().getDetail().getFirstChild().getAttributes()
                .getNamedItem("xsi:type").getNodeValue();
            if (faultType.equals("UnbekannterBenutzerFehler")
                || faultType.equals("AutorisierungsFehler")) {
                return false;
            } else {
                // unreachable
                throw e;
            }
        }

        return true;
    }

    /**
     * Synchronises Courses, Applications and Ranks.
     *
     * <p>
     * Condition for pushing an object to the DoSV: <code>dosvSynctime <= object.timeStamp</code>,
     * where <code>dosvSynctime</code> is the time of the last successful synchronization of dibs
     * with the DoSV and <code>timeStamp</code> is the time of the object's last modification. For
     * inclusion in the sync, the course associated with an application or a ranking has to have the
     * <code>dosv</code>-flag set. The prerequisite for pushing a ranking to the DoSV is that the
     * course is in the admission phase.
     *
     * <p>
     * Synopsis:
     * <ul>
     * <li>push courses</li>
     * <li>for n attempts do</li>
     * <ul>
     * <li>pull applications</li>
     * <li>push applications, return false on version conflict</li>
     * </ul>
     * <li>fail sync if every push returned false</li>
     * <li>push rankings</li>
     * </ul>
     *
     * @throws
     */
    public void synchronize() {
        Date newSyncTime = new Date();
        this.pushCourses();
        boolean applicationsPushed = false;
        // TODO adjust number of retries to minimize the possibility of a RuntimeException
        for (int i = 0; !applicationsPushed && i < 10; i++) {
            this.pullApplications();
            applicationsPushed = this.pushApplications();
        }
        if (!applicationsPushed) {
            throw new RuntimeException("Sync exceeded maximum number of retries.");
        }
        this.pushRankings();
        try {
            Connection db = this.service.getDb();
            this.service.beginTransaction();
            this.service.getQueryRunner().update(db, "UPDATE settings SET dosv_sync_time = ?",
                new Timestamp(newSyncTime.getTime()));
            this.service.getJournal().record(
                ApplicationService.ACTION_TYPE_DOSV_SYNC_SYNCHRONIZED, null, null, null);
            this.service.endTransaction();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private void pushCourses() {
        List<Studienangebot> studienangebote = new ArrayList<>();
        List<Studienpaket> studienpakete = new ArrayList<>();

        Date dosvSyncTime = this.service.getSettings().getDosvSyncTime();
        for (Course course : this.service.getCourses()) {
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
            // NOTE Instantiation is resource intensive so it happens here and not in the
            // constructor
            DosvClient dosvClient = new DosvClient(this.dosvConfig);

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

    private void pullApplications() {
        Date[] updateTime = new Date[1];
        updateTime[0] = this.service.getSettings().getDosvApplicationsServerTime();
        List<Bewerbung> bewerbungen;

        /* SAF 303 */
        // NOTE Instantiation is resource intensive so it happens here and not in the constructor
        DosvClient dosvClient = new DosvClient(this.dosvConfig);
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
            this.service.beginTransaction();
            for (Bewerbung bewerbung : bewerbungen) {
                String newStatus =
                    APPLICATION_STATUS_MAPPING_FROM_DOSV.get(bewerbung.getBearbeitungsstatus());
                Einfachstudienangebotsbewerbung einfachstudienangebotsbewerbung =
                    (Einfachstudienangebotsbewerbung) bewerbung;
                EinfachstudienangebotsSchluessel einfachstudienangebotsSchluessel =
                    einfachstudienangebotsbewerbung.getEinfachstudienangebotsSchluessel();
                if (newStatus == null) {
                    this.service.getQueryRunner().update(this.service.getDb(),
                        "UPDATE application SET dosv_version = ? "
                        + "WHERE course_id = ? AND EXISTS "
                        + "(SELECT id FROM \"user\" WHERE id = user_id AND dosv_bid = ?)",
                        einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel(),
                        einfachstudienangebotsbewerbung.getBewerberId());
                } else {
                    this.service.getQueryRunner().update(this.service.getDb(),
                        "UPDATE application SET status = ?, dosv_version = ?, "
                        + "modification_time = CURRENT_TIMESTAMP WHERE course_id = ? AND EXISTS "
                        + "(SELECT id FROM \"user\" WHERE id = user_id AND dosv_bid = ?)",
                        newStatus, einfachstudienangebotsbewerbung.getVersionSeSt(),
                        einfachstudienangebotsSchluessel.getStudienfachSchluessel(),
                        einfachstudienangebotsbewerbung.getBewerberId());
                }
            }

            this.service.getQueryRunner().update(this.service.getDb(),
                "UPDATE settings SET dosv_remote_applications_pull_time = ?",
                new Timestamp(updateTime[0].getTime()));
            this.service.endTransaction();
        } catch (SQLException e) {
            throw new IOError(e);
        }
    }

    private boolean pushApplications() {
        boolean done = true;
        List<Bewerbung> bewerbungenNeu = new ArrayList<>();
        List<Bewerbung> bewerbungenGeaendert = new ArrayList<>();
        Date dosvSynctime = this.service.getSettings().getDosvSyncTime();

        // TODO should be optimized by WHERE filter
        List<Application> applications = this.service.getApplications();
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
            // NOTE Instantiation is resource intensive so it happens here and not in the
            // constructor
            DosvClient dosvClient = new DosvClient(this.dosvConfig);

            /* SAF 301 */
            List<BewerbungErgebnis> bewerbungErgebnisse =
                dosvClient.uebermittelnNeueBewerbungenAnSeSt(bewerbungenNeu);

            /* SAF 302 */
            bewerbungErgebnisse.addAll(dosvClient
                .uebermittelnGeaenderteBewerbungenAnSeSt(bewerbungenGeaendert));

            for (BewerbungErgebnis bewerbungErgebnis : bewerbungErgebnisse) {
                if (bewerbungErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                    /* "Account zur Löschung vorgesehen" */
                    // checkstyle: ignore EmptyBlock, temporary with TODO
                    if (bewerbungErgebnis.getGrundZurueckweisung().contains("30235")) {
                        // TODO error handling, user notification
                    }
                    /* "Versionskonflikt" */
                    if (bewerbungErgebnis.getGrundZurueckweisung().contains("30233")) {
                        done = false;
                    } else {
                        // unreachable
                        EinfachstudienangebotsbewerbungsSchluessel bewerbungsSchluessel =
                            (EinfachstudienangebotsbewerbungsSchluessel) bewerbungErgebnis
                                .getBewerbungsSchluessel();
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
        // TODO pull webservice call out of loop
        for (Course course : this.service.getCourses()) {
            if (!(course.isDosv()
                && this.service.getSettings().getDosvSyncTime()
                    .before(course.getModificationTime()) && course.isAdmission())) {
                continue;
            }
            Quota quota = course.getAllocationRule().getQuota();
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

            for (Rank rank : quota.getRanking()) {
                Ranglisteneintrag ranglisteneintrag = new Ranglisteneintrag();
                ranglisteneintrag.setBewerberId(rank.getUser().getDosvBid());
                ranglisteneintrag.setRang(rank.getIndex() + 1);
                ranglisteneintrag.setStatus(RanglisteneintragsStatus.BELEGT);
                rangliste.getEintrag().add(ranglisteneintrag);
            }

            RanglisteErgebnis ranglisteErgebnis;
            try {
                // NOTE Instantiation is resource intensive so it happens here and not in
                // the constructor
                ranglisteErgebnis =
                    new DosvClient(this.dosvConfig).uebermittelnRanglistenAnSeSt(
                        Arrays.asList(rangliste)).get(0);
            } catch (BewerberauswahlServiceFehler e) {
                throw new RuntimeException(e);
            }
            if (ranglisteErgebnis.getErgebnisStatus().equals(ZURUECKGEWIESEN)) {
                throw new RuntimeException(ranglisteErgebnis.getGrundZurueckweisung());
                // unreachable
            }
        }
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

    /**
     * Logger for SOAP-Messages. Connects to the webservice <code>Handler</code>-chain and logs the
     * message XML.
     */
    private class SoapLogger implements SOAPHandler<SOAPMessageContext> {
        @Override
        public boolean handleMessage(SOAPMessageContext context) {
            Document xml = context.getMessage().getSOAPPart();
            Transformer transformer;
            try {
                transformer = TransformerFactory.newInstance().newTransformer();
            } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
                // unreachable
                throw new RuntimeException(e);
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            Writer out = new StringWriter();
            try {
                transformer.transform(new DOMSource(xml), new StreamResult(out));
            } catch (TransformerException e) {
                // unreachable
                throw new RuntimeException(e);
            }
            logger.fine(out.toString());

            return true;
        }

        @Override
        public boolean handleFault(SOAPMessageContext context) {
            return this.handleMessage(context);
        }

        @Override
        public Set<QName> getHeaders() {
            return null;
        }

        @Override
        public void close(MessageContext context) { }
    }
}
