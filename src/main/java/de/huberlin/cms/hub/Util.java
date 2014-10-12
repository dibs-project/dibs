/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.HashMap;
import java.util.Map;

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
    
    /**
     * Wandelt ein Instanz von {@code Map<String, Object>} zu 
     * {@code HashMap<String, Object>} um.Erzeugt ein Koppie von original Objekt 
     * wenn es kein Instanz von HashMap ist.
     * 
     * @param map instanz von Map<String, Object>
     * @return {@code HashMap<String, Object}
     */
    public static HashMap<String, Object> convertMapToHashMap(Map<String, Object> map) {
        return (map instanceof HashMap<?, ?>) ? (HashMap<String, Object>) map : 
            new HashMap<String, Object>(map);

    }
    
}
