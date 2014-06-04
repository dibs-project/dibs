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

    /**
     * Initialisiert den User.
     */
    User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
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

    /**
     * Initialisiert den User via Datenbankcursor.
     *
     * @param results Datenbankcursor, der auf eine Zeile aus <code>user</code> verweist
     * @throws SQLException falls ein Datenbankzugriffsfehler auftritt
     */
    User(ResultSet results) throws SQLException {
        this(results.getString("id"), results.getString("name"),
            results.getString("email"));
    }
}
