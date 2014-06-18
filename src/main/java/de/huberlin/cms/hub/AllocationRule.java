/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Vergabeschema.
 *
 * @author Phuong Anh Ha
 */
public class AllocationRule extends HubObject {
    private String name;
    private List<Quota> quotas;

    AllocationRule(String id, String name, ApplicationService service) {
        super(id, service);
        this.name = name;
    }

    AllocationRule(String id, String name, List<Quota> quotas, ApplicationService service) {
        super(id, service);
        this.name = name;
        this.quotas = quotas;
    }

    AllocationRule(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Einstellungen über den Datenbankcursor
        this(results.getString("id"), results.getString("name"), service);
    }

    /**
     * Name des Vergabeschemas.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Liste der Ranglistenköpfen.
     */
    public List<Quota> getQuotas() {
        return this.quotas;
    }
}
