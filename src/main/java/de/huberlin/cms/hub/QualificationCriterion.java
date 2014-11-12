/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.Map;

/**
 * Kriterium, welches die Hochschulzugangsberechtigung abbildet.
 *
 * @author David Koschnick
 */

public class QualificationCriterion extends Criterion  {

    QualificationCriterion(Map<String, Object> args) {
        super(args);
        this.id = "qualification";
    }

    @Override
    public Double evaluate(Application application, Information information) {
        return ((Qualification) information).getGrade();
    }
}
