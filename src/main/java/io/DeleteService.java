package io;

import com.google.api.services.drive.model.Change;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService {

    private static Logger logger;
    private String absolutePath;

    public DeleteService(String absolutePath){
        this.absolutePath = absolutePath;
    }

    public final boolean execute(){
        Path path = FileSystems.getDefault().getPath(absolutePath);

        try{
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            }

            Files.deleteIfExists(path);
        } catch (FileNotFoundException exception) {
            return true;
        } catch (IOException exception) {
            logger.error("Error when deleting %s", path);
            return false;
        }

        return true;
    }

    /**
     * Delete files contains in directory prior deleting it
     * @param path Path
     * @throws IOException
     */
    private void deleteDirectory(Path path) throws IOException {
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
