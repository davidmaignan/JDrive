package io;

import com.google.common.collect.ImmutableList;
import configuration.Configuration;
import database.repository.FileRepository;
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
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-20.
 */
public class TrashedTest {
    private FileSystemInterface fs;
    private Trashed trashed;
    private FileRepository fileRepository;
    private Node fileNode;
    private String path;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        fileRepository = mock(FileRepository.class);
        fileNode = mock(Node.class);

        trashed = new Trashed(fs, fileRepository);
        trashed.setFileId("fileId");

        path = "test";

        init();
    }

    private void init() throws IOException {
        Path folder = fs.getRootPath().resolve(path);
        Files.createDirectories(fs.getRootPath());
        Files.createDirectory(folder);
        Files.write(fs.getRootPath().resolve("file1.txt"), ImmutableList.of("Hello world"), StandardCharsets.UTF_8);
    }

    @Test
    public void testExecuteFolder() throws Exception {
        when(fileRepository.getNodeAbsolutePath(fileNode)).thenReturn(path);
        when(fileRepository.markAsProcessed(fileNode)).thenReturn(true);

        assertTrue(trashed.execute(fileNode));
        assertEquals(1, getList().size());
    }

    @Test
    public void testExecuteFile() throws Exception {
        when(fileRepository.getNodeAbsolutePath(fileNode)).thenReturn("file1.txt");
        when(fileRepository.markAsProcessed(fileNode)).thenReturn(true);


        assertTrue(trashed.execute(fileNode));
        assertEquals(1, getList().size());
    }

    @Test
    public void testExecuteFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(fileNode)).thenReturn(null);

        assertFalse(trashed.execute(fileNode));
        assertEquals(2, getList().size());

        verify(fileRepository, never()).markAsProcessed(fileNode);
    }

    @Test
    public void testWrite() throws Exception{
        assertFalse(trashed.write("path"));
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(trashed.write("path", "path"));
    }


    private List<Path> getList() throws IOException {
        return Files.list(trashed.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}