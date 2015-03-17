/*
 * dibs
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package university.dibs.dibs;

import java.io.IOError;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Gewöhnliche Katze. Demonstriert den dibs-Code-Stil, -Konventionen und häufig verwendete
 * Muster.
 * <p>
 * Beachte auch die Sortierung der Attribute und Methoden.
 * <p>
 * Cat ist ein Datenbankobjekt und eine dazugehörige Tabelle hätte folgende Struktur:
 * <pre>
 * CREATE TABLE cat (
 *     id VARCHAR(256) PRIMARY KEY,
 *     name VARCHAR(256) NOT NULL,
 *     mood INTEGER NOT NULL
 * );
 * </pre>
 *
 * @author Sven Pfaller
 */
public class Cat extends DibsObject {
    public final static String FAVORITE_FOOD = "Cheezburger";

    private static Logger logger = Logger.getLogger("university.dibs.dibs");

    private String name;
    private int mood;

    /**
     * Gibt eine zufällige Speise zurück.
     *
     * @return zufällige Speise
     */
    public static String getRandomFood() {
        return new String[]{"Cheezburger", "Mouse"}[new Random().nextInt(2)];
    }

    Cat(String id, String name, int mood, ApplicationService service) {
        super(id, service);
        this.name = name;
        this.mood = mood;
    }

    Cat(ResultSet results, ApplicationService service) throws SQLException {
        // initialisiert die Katze über den Datenbankcursor
        this(results.getString("id"), results.getString("name"),
            results.getInt("mood"), service);
    }

    /**
     * Füttert die Katze.
     *
     * @param food Name der Speise (z.B. Cheezburger)
     * @param agent ausführender Benutzer
     * @throws IllegalArgumentException wenn <code>food</code> leer ist
     */
    public void feed(String food, User agent) {
        if (food.isEmpty()) {
            throw new IllegalArgumentException("illegal food: empty");
        }

        this.mood += 1;
        try {
            PreparedStatement statement = this.service.getDb().prepareStatement(
                "UPDATE cat SET mood = mood + 1 WHERE id = ?");
            statement.setString(1, this.id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IOError(e);
        }

        // nach dem Essen hat die Katze Energie zum Nachdenken
        think();
    }

    private String think() {
        logger.fine("running cat AI");
        return Cat.FAVORITE_FOOD;
    }

    /**
     * (Ruf-) Name der Katze.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Stimmung, wobei positive Werte gute und negative Werte schlechte Laune bedeuten.
     */
    public double getMood() {
        return this.mood;
    }
}
