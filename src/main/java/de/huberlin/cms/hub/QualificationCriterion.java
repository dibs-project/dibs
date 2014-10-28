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
    QualificationCriterion(String id, Information.Type requiredInformationType,
            ApplicationService service) {
        super("qualification", requiredInformationType, service);
    }

//    QualificationCriterion(Map<String, Object> args) {
//        super((String)args.get("qualification"),
//            (Information.Type)args.get("requiredInformationType"),
//            (ApplicationService)args.get("service"));
//    }

    @Override
    public Double evaluate(Application application, Information information) {
        return ((Qualification) information).getGrade();
    }
}
