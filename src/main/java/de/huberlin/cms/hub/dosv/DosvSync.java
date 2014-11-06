/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.dosv;

import java.util.Properties;

import de.hochschulstart.hochschulschnittstelle.benutzerservicev1_0.BenutzerServiceFehler;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.AutorisierungsFehler;
import de.hochschulstart.hochschulschnittstelle.commonv1_0.UnbekannterBenutzerFehler;
import de.hu_berlin.dosv.DosvClient;
import de.huberlin.cms.hub.ApplicationService;
import de.huberlin.cms.hub.Settings;

/**
 * Synchronisiationsklasse für Studiengänge, Bewerbungen und Ranglisten mit dem DoSV.
 * Jedes Datum wird ausschließlich entweder im System des DoSV oder in HUB verändert.
 *
 * @author Markus Michler
 */
public class DosvSync {
    private Properties dosvConfig;

    public DosvSync(ApplicationService service) {
        dosvConfig = service.getConfig();
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

}
