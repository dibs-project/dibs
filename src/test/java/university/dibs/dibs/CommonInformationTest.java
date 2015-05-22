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

import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.Test;

import university.dibs.dibs.Information;

/**
 * Extension API: Basisklasse für {@link Information}-Tests. Stellt allgemeine Tests der
 * {@link Information}-API bereit.
 */
public abstract class CommonInformationTest extends DibsTest {
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
