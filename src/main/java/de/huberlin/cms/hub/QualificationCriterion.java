/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * Kriterium, welches die Hochschulzugangsberechtigung abbildet.
 *
 * @author David Koschnick
 */

public class QualificationCriterion extends Criterion  {

    QualificationCriterion(String id, Information.Type requiredInformationType,
            ApplicationService service) {
        super("qualification", requiredInformationType, service);
    }

    @Override
    /**
     * Bewertet die Hochschulzugangsberechtigung.
     *
     * @param application Bewerbung
     * @param qualification Hochschulzugangsberechtigung
     * @return Note der Hochschulzugangsberechtigung
     */
    public Double evaluate(Application application, Information information) {
        return ((Qualification) information).getGrade();
    }
}
