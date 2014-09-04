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
     * Basisklasse für Exceptions im Zusammenhang mit Publikationsoperationen.
     *
     * @author Markus Michler
     */
    public abstract static class PublicationException extends HubException {
        protected String objectId;

        public PublicationException(String code, String objectId) {
            super(code);
            this.objectId = objectId;
        }

        public String getObjectId() {
            return objectId;
        }
    }

    /**
     * Exception die auftritt, wenn versucht wurde, ein publiziertes Objekt zu verändern.
     *
     * @author Markus Michler
     */
    public static class PublishedModificationException extends PublicationException {
        public PublishedModificationException(String objectId) {
            super("cannot_modify_published_object", objectId);
        }

        public String getMessage() {
            return "Object '" + objectId + "' has already been published."
                + " Retract publication before modification.";
        }
    }

    /**
     * Exception die auftritt, wenn versucht wird, auf einem unpubliziertem Objekt eine
     * nur für publizierte Objekte vorgesehene Operation durchzuführen.
     *
     * @author Markus Michler
     */
    public static class UnpublishedException extends PublicationException {
        public UnpublishedException(String objectId) {
            super("not_published", objectId);
        }

        public String getMessage() {
            return "Object '" + objectId + "' is unpublished: Cannot perform requested action.";
        }
    }

    /**
     * Exception die auftritt, wenn versucht wird, ein unvollständiges Objekt zu publizieren.
     *
     * @author Markus Michler
     */
    public static class CannotPublishException extends PublicationException {
        private String reason;

        public CannotPublishException(String objectId, String reason) {
            super("unable_to_publish", objectId);
            this.reason = reason;
        }

        public String getMessage() {
            return "Unable to publish object '" + objectId +
                "' in its present state. Reason: " + reason + ".";
        }
    }

    /**
     * Exception die auftritt, wenn eine Publikation nicht mehr rückgängig gemacht werden kann.
     *
     * @author Markus Michler
     */
    public static class CannotRetractException extends PublicationException {
        private String reason;

        public CannotRetractException(String objectId, String reason) {
            super("unable_to_retract", objectId);
        }

        public String getMessage() {
            return "Unable to retract publication of object '"
                + objectId + "'. Reason: " + reason + ".";
        }
    }
}
