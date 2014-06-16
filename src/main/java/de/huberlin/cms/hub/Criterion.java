/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * Kriterium, welches zu die Auswahl- und Zulassungsregeln abbildet.
 *
 * @author David Koschnick
 */
public abstract class Criterion {
    protected String name;
    protected String requiredInformationType;
    protected ApplicationService service;

    /**
     * Initialisiert das Kriterium.
     */
    Criterion(String name, String requiredInformationType,
            ApplicationService service) {
        this.name = name;
        this.requiredInformationType = requiredInformationType;
        this.service = service;
    }

    public Criterion evaluate(Application application, Information information)
    {
        return null;
    };

    /**
     * Name, des Kriteriums.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Für das Kriterium benötigte Informationen.
     */
    public String getRequiredInformationType() {
        return this.requiredInformationType;
    }
}
