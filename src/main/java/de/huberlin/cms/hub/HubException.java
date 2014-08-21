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
public class HubException extends RuntimeException {
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
}
