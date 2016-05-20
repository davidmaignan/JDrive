package io;

import com.google.inject.Inject;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Folder implements WriterInterface{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    private FileSystemInterface fileSystem;
    private Path path;
    private String fileId;

    @Inject
    public Folder(@Real FileSystemInterface fileSystem){
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean write(String pathString) {
        path = fileSystem.getRootPath().resolve(pathString);

        try {
            Files.createDirectory(path);
            return true;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return false;
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
