package drive.change.services;

import drive.change.ChangeStruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Delete a file or folder locally when receiving a delete change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService implements DriveChangeInterface {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    private ChangeStruct changeStruct;

    @Override
    public void setStructure(ChangeStruct structure) {

    }

    public final boolean execute(){
        return true;
//        Path path = null;
//
//        try{
//            path = FileSystems.getDefault().getPath(structure.getNewPath());
//
//            Files.deleteIfExists(path);
//
//            return true;
//        } catch (FileNotFoundException exception) {
//            return false;
//        } catch (IOException exception) {
////            exception.printStackTrace();
//            logger.error(exception.getMessage());
//            return false;
//        }
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
