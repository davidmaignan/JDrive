package io.filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by david on 2016-05-19.
 */
public class FileSystemWrapper implements FileSystemInterface {

    @Override
    public Path getPath(String path) {
        return Paths.get(path);
    }
}
