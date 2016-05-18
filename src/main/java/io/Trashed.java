package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-12.
 */
public class Trashed {
    private static Logger logger = LoggerFactory.getLogger(Trashed.class);

    private FileRepository fileRepository;

    @Inject
    public Trashed(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public boolean execute(Node node){
        boolean result;
        try{
            Path path = Paths.get(fileRepository.getNodeAbsolutePath(node));
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            }

            result = Files.deleteIfExists(path);
            result = true;

        } catch (FileNotFoundException exception) {
            result = true;
            exception.printStackTrace();
        } catch (DirectoryNotEmptyException exception){
            result = true;
            exception.printStackTrace();
        } catch(IOException exception){
            result = true;
            exception.printStackTrace();
        }

        if(result){
            fileRepository.markAsProcessed(node);
        }

        if( ! result){
            logger.error("Error while deleting node for file deleted: " + fileRepository.getTitle(node));
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
}
