package io.filesystem.modules;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import io.filesystem.FileSystemInterface;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by david on 2016-05-19.
 */
public class FileSystemWrapperTest implements FileSystemInterface {

    private FileSystem fs;

    public FileSystemWrapperTest(){
        fs = Jimfs.newFileSystem(Configuration.unix());
    }

    public FileSystem getFileSystem(){
        return fs;
    }

    @Override
    public Path getPath(String path) {
        return fs.getPath(path);
    }
}
