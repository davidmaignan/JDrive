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
        path = fileSystem.getPath(pathString);

        try {
            Path newDir = Files.createDirectory(path);
            return true;
        } catch(FileSystemAlreadyExistsException e){
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
        } catch (Exception exception){
        }

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
