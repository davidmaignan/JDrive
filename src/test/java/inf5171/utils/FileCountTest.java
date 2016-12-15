package inf5171.utils;

import com.google.common.collect.ImmutableList;
import configuration.Configuration;
import inf5171.utils.FileCount;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


import static org.junit.Assert.*;

/**
 * Created by david on 2016-12-14.
 */
public class FileCountTest {
    private FileCount fileCount;
    private FileSystemInterface fs;

    @Before
    public void setUp() throws Exception {
        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        init();

        fileCount = new FileCount(fs, fs.getRootPath());
    }

    @Test
    public void testCompute() throws Exception {
        assertEquals(6, (int)fileCount.compute());
    }

    @Test
    public void testStaticCompute(){
        assertEquals(6, (int)FileCount.compute(fs, fs.getRootPath()));
    }

    private void init() throws IOException {
        Path folder = fs.getRootPath().resolve("folder");
        Files.createDirectories(fs.getRootPath());
        Files.createDirectory(folder);
        Files.write(fs.getRootPath().resolve("file1.txt"), ImmutableList.of("Foo Bar"), StandardCharsets.UTF_8);
        Files.createDirectory(fs.getRootPath().resolve("folder/folder1"));
        Files.write(fs.getRootPath().resolve("folder/folder1/file1"), ImmutableList.of("Foo Bar"), StandardCharsets.UTF_8);
        Files.createDirectory(fs.getRootPath().resolve("folder/folder2"));
        Files.write(fs.getRootPath().resolve("folder/folder2/file2"), ImmutableList.of("Foo Bar"), StandardCharsets.UTF_8);
    }
}