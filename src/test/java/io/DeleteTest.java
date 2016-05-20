package io;

import com.google.common.collect.ImmutableList;
import configuration.Configuration;
import database.repository.ChangeRepository;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-20.
 */
public class DeleteTest {
    private FileSystemInterface fs;
    private Delete delete;
    private ChangeRepository changeRepository;
    private Node fileNode;
    private String path;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        changeRepository = mock(ChangeRepository.class);
        fileNode = mock(Node.class);

        delete = new Delete(fs, changeRepository);
        delete.setFileId("fileId");

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
        when(changeRepository.getNodeAbsolutePath(fileNode)).thenReturn(path);
        when(changeRepository.delete(fileNode)).thenReturn(true);
        assertTrue(delete.execute(fileNode));
        assertEquals(1, getList().size());
    }

    @Test
    public void testExecuteFile() throws Exception {
        when(changeRepository.getNodeAbsolutePath(fileNode)).thenReturn("file1.txt");
        when(changeRepository.delete(fileNode)).thenReturn(true);
        assertTrue(delete.execute(fileNode));
        assertEquals(1, getList().size());
    }

    @Test
    public void testExecuteFails() throws Exception {
        when(changeRepository.getNodeAbsolutePath(fileNode)).thenReturn(null);

        assertFalse(delete.execute(fileNode));
        assertEquals(2, getList().size());
        verify(changeRepository, never()).delete(fileNode);
    }

    @Test
    public void testWrite() throws Exception{
        assertFalse(delete.write("path"));
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(delete.write("path", "path"));
    }


    private List<Path> getList() throws IOException {
        return Files.list(delete.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}