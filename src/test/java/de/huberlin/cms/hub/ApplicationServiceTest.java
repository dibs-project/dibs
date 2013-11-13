/*
 * hub
 */

package de.huberlin.cms.hub;

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
    Properties config;

    @Before
    public void before() throws IOException {
        config = new Properties();
        try {
            config.load(new FileInputStream("hub.properties"));
        } catch (FileNotFoundException e) {
            // skip the tests
            assumeTrue(false);
        }
    }

    @Test
    public void testOpenDatabase() throws SQLException {
        assertNotNull(ApplicationService.openDatabase(config));
    }
}
