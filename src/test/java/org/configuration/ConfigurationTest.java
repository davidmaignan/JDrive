package org.configuration;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Configuration Unit test
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ConfigurationTest {

    @Test(timeout = 1000)
    public void testFileExists() throws Exception{
        Configuration configConfiguration = new Configuration();
    }

    @Test(timeout = 1000)
    public void testPropertyExist() throws Exception{
        Configuration configConfiguration = new Configuration();

        assertEquals("/Users/david/JDrive", configConfiguration.getProperty("rootFolder"));
    }

    @Test(timeout = 1000)
    public void testNoPropertyFound() throws Exception {
        Configuration configConfiguration = new Configuration();

        assertEquals(null, configConfiguration.getProperty("mockProperty"));
    }

    @Test(timeout = 1000)
    public void testDBName() throws Exception {
        Configuration configConfiguration = new Configuration();

        assertEquals("JDrive", configConfiguration.getProperty("dbName"));
    }
}