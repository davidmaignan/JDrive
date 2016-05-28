package io;

import com.google.inject.Inject;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Created by david on 2016-05-19.
 */
public class Move implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(Move.class.getSimpleName());
    private FileSystemInterface fileSystem;

    @Inject
    public Move(@Real FileSystemInterface fileSystem){
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean write(String path) {
        return false;
    }

    @Override
    public boolean write(String oldPathString, String newPathString) {
        try {
            Path oldPath = fileSystem.getRootPath().resolve(oldPathString);
            Path newPath = fileSystem.getRootPath().resolve(newPathString);

            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            return true;

        } catch (IOException exception) {
            logger.error(exception.getMessage());
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    @Override
    public void setFileId(String fileId) {

    }

    @Override
    public FileSystemInterface getFileSystem() {
        return fileSystem;
    }
}
