package de.huberlin.cms.dosv;

import java.util.Properties;

import de.hochschulstart.hochschulschnittstelle.benutzerservicev1_0.BenutzerServiceFehler;
import de.hochschulstart.hochschulschnittstelle.benutzerv1_0.Bewerber;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.AutorisierungsFehler;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.UnbekannterBenutzerFehler;
import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Settings;

/**
 * Synchronisiert Studiengänge, Bewerbungen und Ranglisten mit dem DoSV. Jedes Datum
 * wird ausschließlich entweder im System des DoSV oder lokal geschrieben.
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
     * zusätzliche Autorisierung des Benutzers durch einen Servicestellenmitarbeiter. Der
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

    public DosvSync(ApplicationService service) {
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
            default: /** AKTIV */
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

}
