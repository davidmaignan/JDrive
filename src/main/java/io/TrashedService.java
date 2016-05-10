package io;

import drive.change.ChangeStruct;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TrashedService extends AbstractChangeService {
    public TrashedService(ChangeStruct structure){
        super(structure);
        logger.debug(this.getClass().getSimpleName().toString());
    }

    public final boolean execute(){
        Path path = null;

        try{
            path = FileSystems.getDefault().getPath(structure.getNewPath());

            Files.deleteIfExists(path);

            return true;
        } catch (FileNotFoundException exception) {
            return true;
        } catch (IOException exception) {
//            exception.printStackTrace();
            logger.error(exception.getMessage());
            return false;
        }
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
