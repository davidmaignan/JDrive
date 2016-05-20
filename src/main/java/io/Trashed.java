package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public class Trashed implements WriterInterface{
    private static Logger logger = LoggerFactory.getLogger(Trashed.class);

    private FileSystemInterface fileSystem;
    private FileRepository fileRepository;
    private String fileId;

    @Inject
    public Trashed(@Real FileSystemInterface fileSystem, FileRepository fileRepository){
        this.fileSystem = fileSystem;
        this.fileRepository = fileRepository;
    }

    public boolean execute(Node node){
        boolean result;
        Path path;
        try {
            path = fileSystem.getRootPath().resolve(fileRepository.getNodeAbsolutePath(node));

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

        if(result){
            fileRepository.markAsProcessed(node);
        }

        if( ! result){
            logger.error("Error while trashing a file: " + fileRepository.getTitle(node));
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
    public boolean write(String path) {
        return false;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }

    @Override
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public FileSystemInterface getFileSystem() {
        return fileSystem;
    }
}
