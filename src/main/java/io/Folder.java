package io;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Folder implements WriterInterface{
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Override
    public boolean write(String pathString) {
        Path path = Paths.get(pathString);

        try {
            Path newDir = Files.createDirectory(path);
            return true;
        } catch(FileSystemAlreadyExistsException e){
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public void setFileId(String fileId) {

    }

    @Override
    public boolean delete(String path) {
        File file = new File(path);

        return file.delete();
    }
}
