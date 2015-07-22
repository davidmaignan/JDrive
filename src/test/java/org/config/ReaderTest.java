package org.config;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Reader Unit test
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ReaderTest {

    @Test(timeout = 1000)
    public void testFileExists() throws Exception{
        Reader configReader = new Reader();
    }

    @Test(timeout = 1000)
    public void testPropertyExist() throws Exception{
        Reader configReader = new Reader();

        assertEquals("/Users/david/JDrive", configReader.getProperty("rootFolder"));
    }

    @Test(timeout = 1000)
    public void testNoPropertyFound() throws Exception {
        Reader configReader = new Reader();

        assertEquals(null, configReader.getProperty("mockProperty"));
    }
}