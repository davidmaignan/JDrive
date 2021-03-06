package io;

import com.google.inject.Inject;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public class Delete implements WriterInterface{
    private static Logger logger = LoggerFactory.getLogger(Delete.class);

    private FileSystemInterface fileSystem;

    @Inject
    public Delete(@Real FileSystemInterface fileSystem){
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean write(String pathString) {
        boolean result;
        Path path;
        try{
            path = fileSystem.getRootPath().resolve(pathString);

            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            }

            Files.delete(path);

            result = true;

        } catch (NoSuchFileException exception) {
            result = true;
        } catch (DirectoryNotEmptyException exception){
            result = true;
        } catch (Exception exception){
            result = false;
            logger.error(exception.getMessage());
        }

        if( ! result){
            logger.error("Error while deleting node for file deleted: " + pathString);
        }

        return result;
    }

    private static void deleteDirectory(Path path) throws IOException {
        //Delete files
        Files.list(path).filter( s -> {
            if (Files.isDirectory(s)) return false;
            else return true;
        }).forEach( s -> {
            try {
                Files.deleteIfExists(s);
            } catch (IOException e) {
                logger.error("Error when deleting %s", path);
            }
        });

        //If directory - delete recursively
        Files.list(path).filter( s -> {
            if (Files.isDirectory(s)) return true;
            else return false;
        }).forEach( s -> {
            try {
                deleteDirectory(s);
            } catch (IOException e) {
                logger.error("Error when deleting %s", path);
            }
        });

        Files.deleteIfExists(path);
    }

    @Override
    public boolean write(String oldPath, String newPath) {
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
