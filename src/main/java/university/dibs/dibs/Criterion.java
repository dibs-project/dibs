/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import java.util.Map;

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
    protected Criterion(Map<String, Object> args) {
        this.id = (String) args.get("id");
        this.requiredInformationType = (Information.Type)
            args.get("requiredInformationType");
        this.service = (ApplicationService) args.get("service");
    }

    /**
     * Bewertet die Information.
     * Ein Rueckgabewert von <code>null</code> bedeutet die Information konnte nicht 
     * automatisch bewertet werden.
     *
     * @param application Bewerbung
     * @param information Information, welche bewertet wird
     * @return berechneter Wert für die Information
     */
    public Double evaluate(Application application, Information information) {
        return null;
    }

    /**
     * Eindeutige ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Für das Kriterium benötigte Informationen.
     */
    public Information.Type getRequiredInformationType() { 
        return this.requiredInformationType; 
    }
}
