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

    QualificationCriterion(ApplicationService service) {
        super("qualification", "qualification", service);
    }

    @Override
    public Criterion evaluate(Application application, Information information)
    {
        if (information.getGrade())
        return this.grade;
    };
}
