package configuration;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Configuration Unit test
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ConfigurationTest {
    Configuration configConfiguration;

    @Before
    public void setUp() throws IOException{
        configConfiguration = new Configuration();
    }

    @Test(timeout = 1000)
    public void testJDrivePathExists() throws Exception{
        Configuration configConfiguration = new Configuration();

        assertEquals("/Test/Path", configConfiguration.getProperty("rootFolder"));
    }

    @Test(timeout = 1000)
    public void testNoPropertyFound() throws Exception {
        Configuration configConfiguration = new Configuration();

        assertEquals(null, configConfiguration.getProperty("mockProperty"));
    }

    @Test(timeout = 1000)
    public void testDBNameExists() throws Exception {
        Configuration configConfiguration = new Configuration();

        assertEquals("db_test", configConfiguration.getProperty("dbName"));
    }
}