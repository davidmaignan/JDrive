package io;

import configuration.Configuration;
import drive.api.FileService;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by david on 2016-05-28.
 */
public class FileTest {
    private static Logger logger = LoggerFactory.getLogger(FileTest.class.getSimpleName());
    private File file;
    private FileSystemInterface fs;
    private FileService fileService;
    private String fileId;
    private String path;
    private InputStream input;

    @Before
    public void setUp() throws IOException {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);
        fileService = mock(FileService.class);

        file = new File(fs, fileService);

        fileId = "fileId";
        file.setFileId(fileId);
        path = "file.txt";

        Files.createDirectories(fs.getRootPath());

        input = new ByteArrayInputStream("test data".getBytes());
    }

    @Test
    public void write() throws Exception {
        when(fileService.downloadFile(fileId)).thenReturn(input);

        file.write(path);

        Path filePath = Files.list(file.getFileSystem().getRootPath()).filter( p -> p.getFileName().toString().equals(path)).findFirst().get();

        assertEquals("test data", new String(Files.readAllBytes(filePath)));
    }

    @Test
    public void write1() throws Exception {
        assertFalse(file.write("path", "path"));
    }

    private List<Path> getList() throws IOException {
        return Files.list(file.getFileSystem().getRootPath()).collect(Collectors.toList());
    }

}