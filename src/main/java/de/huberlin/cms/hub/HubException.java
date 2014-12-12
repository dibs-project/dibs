/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * Exception-Basisklasse für HUB-spezifische Anwendungsfälle.
 *
 * @author Markus Michler
 */
@SuppressWarnings("serial")
public abstract class HubException extends RuntimeException {
    protected String code;

    public HubException(String code) {
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
    public static class ObjectNotFoundException extends HubException {
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
     * HUB ist in einem mit der Ausführung der Operation inkompatiblen Zustand.
     *
     * @author Markus Michler
     * @see java.lang.IllegalStateException
     */
    public static class IllegalStateException extends HubException {
        public IllegalStateException(String code) {
            super(code);
        }
    }
}
