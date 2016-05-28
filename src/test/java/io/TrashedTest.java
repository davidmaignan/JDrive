package io;

import com.google.common.collect.ImmutableList;
import configuration.Configuration;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by david on 2016-05-20.
 */
public class TrashedTest {
    private FileSystemInterface fs;
    private Trashed trashed;
    private String path;
    private String pathFile;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        trashed = new Trashed(fs);
        trashed.setFileId("fileId");

        path = "test";
        pathFile = "file1.txt";

        init();
    }

    private void init() throws IOException {
        Path folder = fs.getRootPath().resolve(path);
        Files.createDirectories(fs.getRootPath());
        Files.createDirectory(folder);
        Files.write(fs.getRootPath().resolve(pathFile), ImmutableList.of("Hello world"), StandardCharsets.UTF_8);
    }

    @Test
    public void testWriteFolder() throws Exception{
        assertEquals(2, getList().size());
        assertTrue(trashed.write(path));
        assertEquals(1, getList().size());
    }

    @Test
    public void testWriteFile() throws Exception{
        assertEquals(2, getList().size());
        assertTrue(trashed.write(pathFile));
        assertEquals(1, getList().size());
    }

    @Test
    public void testWriteNotExist() throws Exception{
        assertEquals(2, getList().size());
        assertTrue(trashed.write("notExists"));
        assertEquals(2, getList().size());
    }

    @Test
    public void testWrite2() throws Exception{
        assertFalse(trashed.write("path", "path"));
    }


    private List<Path> getList() throws IOException {
        return Files.list(trashed.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}