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

        public String getObjectId() {
            return objectId;
        }

        public String getMessage() {
            return "Object '" + objectId + "' does not exist.";
        }
    }

    /**
     * Exception die auftritt, wenn versucht wird, ein Objekt zu verändern, das sich in
     * einem mit der Aktion inkompatiblen Zustand befindet.
     *
     * @author Markus Michler
     * @see java.lang.IllegalStateException
     */
    public static class HubObjectIllegalStateException extends HubException {
        /*TODO nicht alle in Frage kommenden Entitäten haben eine objectId, diese muss evtl.
          später entfernt werden. Dann existiert aber kein Unterschied zu
          java.lang.IllegalStateException */
        protected String objectId;

        public HubObjectIllegalStateException(String objectId) {
            super("object_illegal_state");
            this.objectId = objectId;
        }

        public String getObjectId() {
            return objectId;
        }

        public String getMessage() {
            return "The attempted action on object '" + objectId + "' cannot be performed.";
        }
    }
}
