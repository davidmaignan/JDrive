package io;

import configuration.Configuration;
import io.filesystem.FileSystemInterface;
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
 * Created by david on 2016-05-19.
 */
public class DocumentTest {
    Document document;
    FileSystemInterface fs;

    @Before
    public void setUp(){

        try {
            Configuration configuration = new Configuration();
            fs = new FileSystemWrapperTest(configuration);
            document = new Document(fs);
            document.setFileId("fileId");

            Files.createDirectories(fs.getRootPath());
        } catch (IOException exception){

        }
    }

    @Test
    public void write() throws Exception {
        assertTrue(document.write("document1"));

        List<Path> list = Files.list(document.getFileSystem().getRootPath()).collect(Collectors.toList());

        assertEquals(1, list.size());
        String content = new String(Files.readAllBytes(list.get(0)));
        assertEquals(document.getContent().trim(), content.trim());
    }

    @Test
    public void write1() throws Exception {
        assertFalse(document.write("document1", "document2"));
    }

    @Test
    public void writeFails() throws Exception {
        assertFalse(document.write("folderNotExists/document1"));
    }
}