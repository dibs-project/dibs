/*
 * HUB
 */

package de.huberlin.cms.hub;

import java.sql.SQLException;

import org.junit.Test;

public class ApplicationServiceTest extends HubTest {
    @Test
    public void testGetApplicant() throws SQLException {
        service.getApplicant(APPLICANT_ID);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetApplicantInvalidId() throws SQLException {
        service.getApplicant(9999999);
    }
}
