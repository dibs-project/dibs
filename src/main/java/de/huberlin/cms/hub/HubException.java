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

    public HubException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * Wird von get-Methoden ausgelöst, wenn kein Objekt mit der übergebenen ID in der
     * Datenbank existiert.
     *
     * @author Markus Michler
     */
    public static class ObjectNotFoundException extends HubException {
        public ObjectNotFoundException(String objectId) {
            super("object_not_found", "Object '" + objectId
                + "' does not exist in the database.");
        }
    }

    /**
     * Basisklasse für Exceptions bezüglich Publikationsoperationen.
     *
     * @author Markus Michler
     */
    public abstract static class PublicationException extends HubException {
        public PublicationException(String code, String message) {
            super(code, message);
        }
    }

    /**
     * Wird geworfen, wenn versucht wird, ein publiziertes Objekt zu verändern.
     *
     * @author Markus Michler
     */
    public static class PublishedModificationException extends PublicationException {
        public PublishedModificationException(String objectId) {
            super("cannot_modify_published_object", "Object '" + objectId +
                "' has already been published. Retract publication before modification.");
        }
    }

    /**
     * Wird geworfen, wenn versucht wird, auf einem unpubliziertem Objekt eine nur für
     * publizierte Objekte vorgesehene Operation durchzuführen.
     *
     * @author Markus Michler
     */
    public static class UnpublishedException extends PublicationException {
        public UnpublishedException(String objectId) {
            super("not_published", "Object '" + objectId +
                "' is unpublished: Cannot perform requested action.");
        }
    }

    /**
     * Wird geworfen, wenn versucht wird, ein unvollständiges Objekt zu publizieren.
     *
     * @author Markus Michler
     */
    public static class CannotPublishException extends PublicationException {
        public CannotPublishException(String objectId, String reason) {
            super("unable_to_publish", "Unable to publish object '" + objectId +
                "' in its present state. Reason: " + reason + ".");
        }
    }

    /**
     * Wird geworfen, wenn eine Publikation nicht mehr rückgängig gemacht werden kann.
     *
     * @author Markus Michler
     */
    public static class CannotRetractException extends PublicationException {
        public CannotRetractException(String objectId, String reason) {
            super("unable_to_retract", "Unable to retract publication of object '"
                + objectId + "'. Reason: " + reason + ".");
        }
    }
}
