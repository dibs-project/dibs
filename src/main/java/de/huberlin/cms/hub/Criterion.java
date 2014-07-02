/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * Kriterium, welches die Auswahl- und Zulassungsregeln abbildet.
 *
 * @author David Koschnick
 */
public abstract class Criterion {
    protected String id;
    protected Information.Type requiredInformationType;
    protected ApplicationService service;

    /**
     * Initialisiert das Kriterium.
     */
    protected Criterion(String id, Information.Type requiredInformationType,
            ApplicationService service) {
        this.id = id;
        this.requiredInformationType = requiredInformationType;
        this.service = service;
    }

    /**
     * Bewertet die Information.
     *
     * @param application Bewerbung
     * @param information Information, welche bewertet wird
     * @return berechneter Wert für die Information
     */
    public Double evaluate(Application application, Information information) {
        return null;
    };

    /**
     * Für das Kriterium benötigte Informationen.
     */
    public Information.Type getRequiredInformationType() { 
        return this.requiredInformationType; 
    }

    /**
     * Beschreibung eines Kriteriumtyps.
     *
     * @author David Koschnick
     */
    public static abstract class Type {
        protected String id;

        protected Type(String id) {
            this.id = id;
        }

        /**
         * Erstellt eine neue Instanz dieses Kriteriumtyps
         *
         * @param id ID, des Kriteriums
         * @param requiredInformationType Typ der Information
         * @param service Bewerbungsdienst
         * @return neues Kriterium
         */
        public abstract Criterion newInstance(String id,
                Information.Type requiredInformationType, ApplicationService service);

        /**
         * Eindeutige ID.
         */
        public String getId() {
            return this.id;
        }
    }
}
