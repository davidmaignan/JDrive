package io.filesystem;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * Created by david on 2016-05-20.
 */
public class FileSystemWrapperTest implements FileSystemInterface {
    private static Logger logger = LoggerFactory.getLogger(FileSystemWrapperTest.class.getSimpleName());
    private Path root;
    private FileSystem fs;
    private configuration.Configuration config;

    @Inject
    public FileSystemWrapperTest(configuration.Configuration config){
        fs = Jimfs.newFileSystem(Configuration.unix());
        root = fs.getPath(config.getRootFolder());
    }

    @Override
    public FileSystem getFileSystem(){
        return fs;
    }

    @Override
    public Path getPath(String path) {
        return root.resolve(path);
    }

    @Override
    public Path getRootPath() {
        return root;
    }
}
