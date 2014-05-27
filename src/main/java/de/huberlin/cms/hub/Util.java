/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * TODO: dokumentieren
 *
 * @author Sven Pfaller
 * @author Phuong Anh Ha
 */
public class Util {
    /**
     * TODO: dokumentieren
     * @see Enum#valueOf
     */
    public static <T extends Enum<T>> T valueOfEnum(Class<T> enumType, String name) {
        return name != null ? Enum.valueOf(enumType, name) : null;
    }
}
