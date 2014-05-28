/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Benutzer, der mit dem Bewerbungssystem interagiert.
 *
 * @author Sven Pfaller
 */
public class User {
    private String id;
    private String name;
    private String email;

    User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    User(ResultSet results) throws SQLException {
        // initialisiert den User über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getString("email"));
    }

    /**
     * Eindeutige ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Name, mit dem der Benutzer von HUB angesprochen wird.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Email-Adresse.
     */
    public String getEmail() {
        return this.email;
    }
}
