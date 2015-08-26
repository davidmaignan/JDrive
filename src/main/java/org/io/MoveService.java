package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.db.Fields;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Move a file or directory in file system when receiving a change event from api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveService extends ModifiedService implements ChangeInterface {
    private Change change;

    public MoveService(){}

    @Override
    public boolean execute() throws IOException {
        Node oldVertex = dbService.getNodeById(change.getFileId());
        Node newVertex = dbService.getNodeById(change.getFile().getParents().get(0).getId());

        String oldPathString = oldVertex.getProperty(Fields.PATH).toString();
        Path oldPath = FileSystems.getDefault().getPath(oldPathString);

        String newPathString = newVertex.getProperty(Fields.PATH) + "/" + change.getFile().getTitle();
        Path newPath = FileSystems.getDefault().getPath(newPathString);

        System.out.println(oldPathString + " : " + newPathString);

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

        return this.updateDB();
    }
}
