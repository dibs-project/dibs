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

    QualificationCriterion(String name, Information.Type requiredInformationType,
            ApplicationService service) {
        super("qualification",requiredInformationType, service);
    }

    /**
     * Bewertet die Hochschulzugangsberechtigung.
     *
     * @param application Bewerbung
     * @param qualification Hochschulzugangsberechtigung
     * @return Note der Hochschulzugangsberechtigung
     */
    public Double evaluate(Application application, Qualification qualification)
    {
        return qualification.getGrade();
    };

    /**
     * Beschreibung des Kriteriumtyps Hochschulzugangsberechtigung.
     *
     * @author David Koschnick
     */
    public static class Type extends Criterion.Type {
        /**
         * Initialisiert den Kriteriumstyp.
         */
        public Type() {
            super("qualification");
        }

        @Override
        public Criterion newInstance(String id, Information.Type requiredInformationType,
                ApplicationService service) {
            return new QualificationCriterion("qualification", requiredInformationType,
                    service);
        }
    }
}
