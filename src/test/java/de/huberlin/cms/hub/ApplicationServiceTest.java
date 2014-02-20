/*
 * HUB
 * Copyright (C) 2014 Humboldt-Universität zu Berlin
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

public class ApplicationServiceTest extends HubTest {
    @Test
    public void testSetSemester() {
        String semester = "2222SS";
        service.setSemester(semester);
        assertEquals(semester, service.getSettings().getSemester());
    }

    @Test
    public void testGetSettings() {
        service.getSettings();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetApplicantInvalidId() throws SQLException {
        service.getApplicant(9999999);
    }
}
