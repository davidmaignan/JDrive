package org.io;

import com.google.api.services.drive.model.Change;
import database.neo4j.DatabaseService;
import database.Fields;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;

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
        Node node = dbService.getNodeById(change.getFileId());

        String absolutePath = node.getProperty(Fields.PATH).toString();

        Path path = FileSystems.getDefault().getPath(absolutePath);

        try{
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            }

            Files.deleteIfExists(path);
            dbService.delete(change.getFileId());
        }catch (IOException exception) {
            logger.error("Error when deleting %s", path);
        return false;
        }

        return true;
    }

    @Override
    public Change getChange() {
        return null;
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
