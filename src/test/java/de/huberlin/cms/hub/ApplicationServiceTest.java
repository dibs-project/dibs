/*
 * hub
 */

package de.huberlin.cms.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;

public class ApplicationServiceTest {
    ApplicationService service;
    Properties config;

    @Before
    public void before() throws IOException, SQLException {
        config = new Properties();
        try {
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // skip the tests
            assumeTrue(false);
        }
        service = new ApplicationService(ApplicationService.openDatabase(config), config);
    }

    @Test
    public void testOpenDatabase() throws SQLException {
        assertNotNull(ApplicationService.openDatabase(config));
    }

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
        service.getApplicant(100);
    }

    @Test(expected=IllegalArgumentException.class)
    public void testGetApplicantInvalidId() throws SQLException {
        service.getApplicant(9999999);
    }
}
