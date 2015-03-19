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

/**
 * Exception-Basisklasse für dibs-spezifische Anwendungsfälle.
 *
 * @author Markus Michler
 */
@SuppressWarnings("serial")
public abstract class DibsException extends RuntimeException {
    protected String code;

    public DibsException(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Fehler, der anzeigt, dass ein Objekt nicht im System existiert.
     * Wird vorallem von get-Methoden ausgelöst.
     *
     * @author Markus Michler
     */
    public static class ObjectNotFoundException extends DibsException {
        private String objectId;

        public ObjectNotFoundException(String objectId) {
            super("object_not_found");
            this.objectId = objectId;
        }

        /**
         * The object's ID or a parameter used to identify the object in a given context.
         */
        public String getObjectId() {
            return objectId;
        }

        public String getMessage() {
            return "Object '" + objectId + "' does not exist.";
        }
    }

    /**
     * Zeigt an, dass eine Methode zu einem unzulässigen Zeitpunkt aufgerufen wurde:
     * dibs ist in einem mit der Ausführung der Operation inkompatiblen Zustand.
     *
     * @author Markus Michler
     * @see java.lang.IllegalStateException
     */
    public static class IllegalStateException extends DibsException {
        public IllegalStateException(String code) {
            super(code);
        }
    }
}
