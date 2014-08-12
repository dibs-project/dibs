/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

/**
 * Extension API: Basisklasse für {@link Information}-Tests. Stellt allgemeine Tests der
 * {@link Information}-API bereit.
 */
public abstract class CommonInformationTest extends HubTest {
    /**
     * Informationstyp, der getestet wird. Muss von der Unterklasse in {@link @Before}
     * gesetzt werden.
     */
    protected Information.Type informationType;

    /**
     * Gültige Argumente für <code>create</code> des Informationstyps, der getestet wird.
     * Muss von der Unterklasse in {@link @Before} gesetzt werden.
     */
    protected HashMap<String, Object> createArgs;

    @Test
    public void testTypeCreate() {
        Information information =
            this.informationType.create(this.createArgs, this.user, null);
        assertTrue(this.user.getInformationSet(null).contains(information));
    }

    @Test
    public void testTypeCreateImproperArgsKeys() {
        this.exception.expect(IllegalArgumentException.class);
        this.exception.expectMessage("args");
        HashMap<String, Object> args = new HashMap<String, Object>();
        args.put("_foo", null);
        this.informationType.create(args, this.user, null);
    }
}
