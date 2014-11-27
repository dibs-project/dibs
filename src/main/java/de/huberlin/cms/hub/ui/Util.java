/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub.ui;

import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

/**
 * Collection of utilities.
 *
 * @author Sven Pfaller
 */
public class Util {
    /**
     * Checks if a given form contains the required data. Throws an
     * {@link IllegalAccessException} if a value is missing.
     *
     * @param form form to check
     * @param requiredKeys keys of the required data
     * @throws IllegalArgumentException if a value is missing
     */
    public static void checkContainsRequired(MultivaluedMap<String, String> form,
            Set<String> requiredKeys) {
        for (String key : requiredKeys) {
            String value = form.getFirst(key);
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException("form_" + key + "_missing");
            }
        }
    }
}
