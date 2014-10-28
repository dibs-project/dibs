/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

    void assignInformation(Information information) {
        this.informationId = information.getId();
        this.value = this.getCriterion().evaluate(this.getApplication(), information);
        this.status = STATUS_EVALUATED;

        try {
            PreparedStatement statement = this.service.getDb().prepareStatement(
                "UPDATE evaluation SET information_id = ?, value = ?, status = ? WHERE id = ?");
            statement.setString(1, this.informationId);
            statement.setObject(2, this.value);
            statement.setString(3, this.status);
            statement.setString(4, this.id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOError(e);
        }
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
     * ID des Kriteriums.
     */
    public String getCriterionId() {
        return this.criterionId;
    }

    /**
     * Zugeordnete Information. <code>null</code>, wenn noch keine Information verknüpft
     * wurde.
     */
    public Information getInformation() {
        return this.informationId != null ?
            this.service.getInformation(this.informationId) : null;
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
