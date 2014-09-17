/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * TODO: dokumentieren
 *
 * @author Sven Pfaller
 */
public class Util {
    /**
     * Prüft ob ein Wert in einem bestimmten Bereich liegt.
     *
     * @param value Wert, der geprüft werden soll
     * @param min Minimalwert (inklusive)
     * @param max Maximalwert (inklusive)
     * @return <code>true</code> wenn der Wert im definierten Bereich liegt, ansonsten
     *     <code>false</code>
     */
    public static <T extends Comparable<T>> boolean isInRange(T value, T min, T max) {
        return value.compareTo(min) >= 0 && value.compareTo(max) <= 0;
    }
    
    public void setHubObjectHanlder(ApplicationService service) {
        String id;
    }
}
