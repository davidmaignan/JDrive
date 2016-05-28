package io;

import configuration.Configuration;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);
        folder = new Folder(fs);

        Files.createDirectories(fs.getRootPath());
    }

    @Test
    public void testWrite() throws Exception {
        folder.setFileId("fileId");
        assertTrue(folder.write("test"));
        assertEquals(1, getList().size());
        assertEquals(fs.getPath("test"), folder.getPath());
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(folder.write("test", "test"));
    }

    @Test
    public void testWriteFolderAlreadyExistsException() throws IOException {
        assertTrue(folder.write("test"));
        assertTrue(folder.write("test"));

        assertEquals(1, getList().size());
    }

    @Test
    public void testWriteFolderRecursively() throws IOException {
        assertTrue(folder.write("test/folder"));
        //@todo check why not creating 2 folders
        assertEquals(1, getList().size());
    }

    private List<Path> getList() throws IOException {
        return Files.list(folder.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}