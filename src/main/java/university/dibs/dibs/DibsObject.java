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

package university.dibs.dibs;

import java.util.Map;

/**
 * Objekt im dibs-Universum.
 *
 * @author Sven Pfaller
 */
public abstract class DibsObject {
    protected final String id;
    protected final ApplicationService service;

    /**
     * Gibt die ID des Objekts zurück. Dabei werden <code>null</code>-Werte ignoriert.
     *
     * @param object Objekt oder <code>null</code>
     * @return ID des Objekts oder <code>null</code> wenn das Objekt <code>null</code> ist
     */
    public static String getId(DibsObject object) {
        return object != null ? object.getId() : null;
    }

    /**
     * Initialisert das Objekt.
     */
    protected DibsObject(Map<String, Object> args) {
        this.id = (String) args.get("id");
        this.service = (ApplicationService) args.get("service");
    }

    /**
     * Testet ob ein anderes Objekt diesem "gleicht". Zwei dibs-Objekte sind gleich, wenn
     * sie die selbe ID haben.
     *
     * @see Object#equals()
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof DibsObject && this.id.equals(((DibsObject) obj).id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s[id=%s]", this.getClass().getSimpleName(), this.id);
    }

    /**
     * Eindeutige ID.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Zugehöriger Bewerbungsdienst.
     */
    public ApplicationService getService() {
        return this.service;
    }
}
