package io;

import com.google.inject.Inject;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Root implements WriterInterface{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private FileSystemInterface fileSystem;
    private Path path;
    private String fileId;
    private boolean alreadyExists;

    @Inject
    public Root(@Real FileSystemInterface fileSystem){
        this.fileSystem = fileSystem;
        alreadyExists = false;
    }

    @Override
    public boolean write(String pathString) {
        path = fileSystem.getRootPath();

        try {
            Files.createDirectory(path);
            return true;
        } catch (FileAlreadyExistsException exception) {
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return false;
    }

    public boolean exists(){
        return Files.exists(fileSystem.getRootPath());
    }

    public boolean createIfNotExists() {
        return exists() || write("");
    }

    public FileSystemInterface getFileSystem(){
        return fileSystem;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }

    @Override
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public Path getPath(){
        return path;
    }
}
