/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Regel, nach der Studienplätze für ein Angebot an die Bewerber vergeben werden
 * (Vergabeschema).
 *
 * @author Phuong Anh Ha
 */
public class AllocationRule extends HubObject {

    AllocationRule(String id, ApplicationService service) {
        super(id, service);
    }

    AllocationRule(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert das Vergabeschema über den Datenbankcursor
        this(results.getString("id"), service);
    }
}
