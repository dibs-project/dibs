/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

/**
 * Objekt im HUB-Universum.
 *
 * @author Sven Pfaller
 */
public abstract class HubObject {
    protected String id;
    protected ApplicationService service;

    /**
     * Gibt die ID des Objekts zurück. Dabei werden <code>null</code>-Werte ignoriert.
     *
     * @param object Objekt oder <code>null</code>
     * @return ID des Objekts oder <code>null</code> wenn das Objekt <code>null</code> ist
     */
    public static String getId(HubObject object) {
        return object != null ? object.getId() : null;
    }

    /**
     * Initialisert das Objekt.
     */
    protected HubObject(String id, ApplicationService service) {
        this.id = id;
        this.service = service;
    }

    /**
     * Testet ob ein anderes Objekt diesem "gleicht". Zwei HUB-Objekte sind gleich, wenn
     * sie die selbe ID haben.
     *
     * @see Object#equals()
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof HubObject && this.id.equals(((HubObject) obj).id);
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
