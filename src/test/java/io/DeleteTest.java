package io;

import com.google.common.collect.ImmutableList;
import configuration.Configuration;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * Created by david on 2016-05-20.
 */
public class DeleteTest {
    private static Logger logger = LoggerFactory.getLogger(DeleteTest.class.getSimpleName());
    private FileSystemInterface fs;
    private Delete delete;
    private Node fileNode;
    private String path;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);
        
        delete = new Delete(fs);
        delete.setFileId("fileId");

        path = "test";

        init();
    }

    private void init() throws IOException {
        Path folder = fs.getRootPath().resolve(path);
        Files.createDirectories(fs.getRootPath());
        Files.createDirectory(folder);
        Files.write(fs.getRootPath().resolve("file1.txt"),
                ImmutableList.of("Hello world"), StandardCharsets.UTF_8);
    }

    @Test
    public void testWrite() throws Exception{
        assertEquals(2, getList().size());
        assertTrue(delete.write(path));
        assertEquals(1, getList().size());
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(delete.write("path", "path"));
    }


    private List<Path> getList() throws IOException {
        return Files.list(delete.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}