package io;

import io.filesystem.modules.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * Created by david on 2016-05-19.
 */
public class FolderTest {
    private static Logger logger = LoggerFactory.getLogger(FolderTest.class.getSimpleName());
    Folder folder;
    FileSystemWrapperTest fs;

    @Before
    public void setUp() throws Exception {
        fs = new FileSystemWrapperTest();
        folder = new Folder(fs);
    }

    @Test
    public void testWrite() throws Exception {
        assertTrue(folder.write("/root"));
    }

    @Test
    public void testWriteFileAlreadyExistsException(){
        assertTrue(folder.write("/root"));
        assertFalse(folder.write("/root"));
    }
}