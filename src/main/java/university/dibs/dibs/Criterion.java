/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
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
