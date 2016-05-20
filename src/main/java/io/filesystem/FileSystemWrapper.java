package io.filesystem;

import com.google.inject.Inject;
import configuration.Configuration;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by david on 2016-05-19.
 */
public class FileSystemWrapper implements FileSystemInterface {
    private Path root;

    @Inject
    public FileSystemWrapper(Configuration configuration){
        root = Paths.get(configuration.getRootFolder());
    }

    @Override
    public Path getPath(String path) {
        return root.resolve(path);
    }

    @Override
    public Path getRootPath() {
        return root;
    }

    @Override
    public FileSystem getFileSystem() {
        return null;
    }
}
