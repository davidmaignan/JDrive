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
public class MoveTest {
    private FileSystemInterface fs;
    private Move move;

    private String oldPath;
    private String newPath;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);


        move = new Move(fs);
        move.setFileId("fileId");

        oldPath = "folder1";
        newPath = "folder2";

        init();
    }

    private void init() throws IOException {
        Path folder = fs.getRootPath().resolve(oldPath);
        Files.createDirectories(fs.getRootPath());
        Files.createDirectory(folder);
        Files.write(folder.resolve("file1.txt"), ImmutableList.of("Hello world"), StandardCharsets.UTF_8);
    }

    @Test
    public void testWrite() throws Exception {
        assertTrue(move.write(oldPath, newPath));
        List<Path> listFolder = getList();
        assertEquals(1, listFolder.size());
        assertEquals("/Test/Path/folder2", listFolder.get(0).toString());

        List<Path> listFile = Files.list(listFolder.get(0)).collect(Collectors.toList());
        assertEquals(1, listFile.size());
        assertEquals("/Test/Path/folder2/file1.txt", listFile.get(0).toString());
    }

    @Test
    public void testWriteFails() throws Exception {
        assertFalse(move.write("folderNotExists", newPath));

        List<Path> listFolder = getList();
        assertEquals(1, listFolder.size());
        assertEquals("/Test/Path/folder1", listFolder.get(0).toString());

        List<Path> listFile = Files.list(listFolder.get(0)).collect(Collectors.toList());
        assertEquals(1, listFile.size());
        assertEquals("/Test/Path/folder1/file1.txt", listFile.get(0).toString());
    }

    @Test
    public void testWriteFails2() throws Exception {
        assertFalse(move.write(null, newPath));

        List<Path> listFolder = getList();
        assertEquals(1, listFolder.size());
        assertEquals("/Test/Path/folder1", listFolder.get(0).toString());

        List<Path> listFile = Files.list(listFolder.get(0)).collect(Collectors.toList());
        assertEquals(1, listFile.size());
        assertEquals("/Test/Path/folder1/file1.txt", listFile.get(0).toString());
    }

    @Test
    public void write1() throws Exception {
        assertFalse(move.write("path"));
    }

    private List<Path> getList() throws IOException {
        return Files.list(move.getFileSystem().getRootPath()).collect(Collectors.toList());
    }
}