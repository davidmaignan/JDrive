package io;

import configuration.Configuration;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by david on 2016-05-21.
 */
public class RootTest {
    Root root;
    FileSystemWrapperTest fs;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);
        root = new Root(fs);

        Files.createDirectories(fs.getRootPath());
    }

    @Test
    public void testWrite() throws Exception {
        root.setFileId("fileId");
        assertTrue(root.write(""));
        assertEquals(0, getList().size());
        assertEquals(fs.getPath(""), root.getPath());
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(root.write("test", "test"));
    }

    @Test
    public void testExists() throws IOException {
        root.write("");
        assertTrue(root.exists());
    }

    @Test
    public void testCreateIfNotExists(){
        assertTrue(root.createIfNotExists());
    }

    private List<Path> getList() throws IOException {
        return Files.list(root.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}