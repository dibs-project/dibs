/*
 * HUB
 * Copyright (C) 2014  Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.logging.Logger; // TODO

/**
 * Gewöhnliche Katze. Dient dazu unseren Code-Stil und unsere Konventionen zu
 * demonstrieren.
 * <p>
 * Anmerkung: Beachte die Sortierung der Attribute / Methoden.
 *
 * @author pfallers
 */
public class Cat {
    public final static int TAIL_COUNT = 1;

    private static Logger log;// = LoggerFactory.getLogger(Cat.class);

    public String name;

    public Cat(String name) {
        this.name = name;
    }

    /**
     * Bewegt die Katze zu einem spezifizierten Ort.
     *
     * @param place
     * @throws IllegalArgumentException
     */
    public void move(String place) throws IllegalArgumentException {
        think();
    }

    /**
     * (Ruf-) Name der Katze.
     */
    public String getName() {
        return name;
    }

    private String think() {
        return "Mouse";
    }
}
