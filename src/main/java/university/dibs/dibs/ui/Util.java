/*
 * dibs
 * Copyright (C) 2015 Humboldt-Universität zu Berlin
 * 
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>
 */

package university.dibs.dibs.ui;

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
