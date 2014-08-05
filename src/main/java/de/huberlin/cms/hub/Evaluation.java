/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.HashMap;

/**
 * Bewertung eines Bewerbers (bzw.&nbsp;von Informationen eines Bewerbers) anhand eines
 * Kriteriums für eine Bewerbung.
 *
 * @author Sven Pfaller
 */
public class Evaluation extends HubObject {
    /** Status: Information fehlt noch, bzw.&nbsp;wurde noch nicht zugeordnet. */
    public final static String STATUS_INFORMATION_MISSING = "information_missing";
    /** Status: Bewertung noch nicht durchgeführt. */
    public final static String STATUS_UNEVALUATED = "unevaluated";
    /** Status: Bewertung durchgeführt. */
    public final static String STATUS_EVALUATED = "evaluated";

    private final String applicationId;
    private final String criterionId;
    private String informationId;
    private Double value;
    private String status;

    Evaluation(HashMap<String, Object> args) {
        super((String) args.get("id"), (ApplicationService) args.get("service"));
        this.applicationId = (String) args.get("application_id");
        this.criterionId = (String) args.get("criterion_id");
        this.informationId = (String) args.get("information_id");
        this.value = (Double) args.get("value");
        this.status = (String) args.get("status");
    }

    /**
     * Bewerbung, zu der diese Bewertung gehört.
     */
    public Application getApplication() {
        return this.service.getApplication(this.applicationId);
    }

    /**
     * Kriterium, auf das sich die Bewertung bezieht.
     */
    public Criterion getCriterion() {
        return this.service.getCriteria().get(this.criterionId);
    }

    /**
     * Zugeordnete Information. <code>null</code>, wenn noch keine Information verknüpft
     * wurde.
     */
    public Information getInformation() {
        return this.service.getInformation(this.informationId);
    }

    /**
     * Berechneter Wert. <code>null</code>, wenn noch keine Bewertung durchgeführt wurde.
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * Status der Bewertung.
     */
    public String getStatus() {
        return this.status;
    }
}
