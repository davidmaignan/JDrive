package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.db.Fields;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.*;

/**
 * Delete a file or folder locally when receiving a deletion change from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService extends ModifiedService implements ChangeInterface {

    private static Logger logger;
    private DatabaseService dbService;
    private Change change;

    public DeleteService(){}

    @Override
    public final boolean execute(){
//        Vertex vertex = dbService.getVertex(change.getFileId());
//
//        String absolutePath = vertex.getProperty(Fields.PATH);
//
//        Path path = FileSystems.getDefault().getPath(absolutePath);
//
//        try{
//            if (Files.isDirectory(path)) {
//                deleteDirectory(path);
//            }
//
//            Files.deleteIfExists(path);
//            dbService.delete(vertex);
//        }catch (IOException exception) {
//            logger.error("Error when deleting %s", path);
//        return false;
//        }

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
