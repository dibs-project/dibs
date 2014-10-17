/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.dosv;

import de.huberlin.cms.hub.HubException;

/**
 * Fehler, der bei Problemen mit der Authentifikation im System des DoSV mittels BID und BAN
 * ausgelöst wird.
 *
 * @author Markus Michler
 */
@SuppressWarnings("serial")
public class DosvAuthenticationException extends HubException {
    private String message;

    public DosvAuthenticationException(String code, String message) {
        super(code);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
