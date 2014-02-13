/*
 * HUB
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.Test;

public class ApplicationServiceTest extends HubTest {
    @Test
    public void testSetSemester() throws SQLException {
        String semester = "2222SS";
        service.setSemester(semester);
        assertEquals(semester, service.getSettings().getSemester());
    }

    @Test
    public void testGetSettings() throws SQLException {
        service.getSettings();
    }

    @Test
    public void testGetApplicant() throws SQLException {
        service.getApplicant(APPLICANT_ID);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetApplicantInvalidId() throws SQLException {
        service.getApplicant(9999999);
    }
}
