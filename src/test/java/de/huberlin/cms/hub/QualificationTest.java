/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import java.util.HashMap;

import org.junit.Before;

/**
 * @author Sven Pfaller
 */
public class QualificationTest extends CommonInformationTest {
    @Before
    public void before() {
        this.informationType = new Qualification.Type();
        this.createArgs = new HashMap<String, Object>();
        this.createArgs.put("grade", 4.0);
    }
}
