package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Vertex;
import org.db.DatabaseService;
import org.db.Fields;
import org.io.ChangeInterface;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Move a file or directory when a parent is modified
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveService implements ChangeInterface {
    private final DatabaseService dbService;
    private Change change;

    @Inject
    public MoveService(DatabaseService dbService    ) {
        this.dbService = dbService;
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public void execute() throws IOException {
        Vertex oldVertex = dbService.getVertex(change.getFileId());
        Vertex newVertex = dbService.getVertex(change.getFile().getParents().get(0).getId());

        String oldPathString = oldVertex.getProperty(Fields.PATH) + "/" + change.getFile().getTitle();
        Path oldPath = FileSystems.getDefault().getPath(oldPathString);

        String newPathString = newVertex.getProperty(Fields.PATH) + "/" + change.getFile().getTitle();
        Path newPath = FileSystems.getDefault().getPath(newPathString);

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
    }
}
