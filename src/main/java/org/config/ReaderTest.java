package org.config;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test property reader
 */
public class ReaderTest {

    @Test(timeout = 1000)
    public void testFileExists() throws Exception{
        Reader configReader = new Reader();
    }

    @Test(timeout = 1000)
    public void testPropertyExist() throws Exception{
        Reader configReader = new Reader();

        assertEquals("JDrive", configReader.getProperty("rootFolder"));
    }

    @Test(timeout = 1000, expected = Exception.class)
    public void testNoPropertyFound() throws Exception {
        Reader configReader = new Reader();

        configReader.getProperty("mockProperty");
    }
}