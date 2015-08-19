package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Vertex;
import org.db.DatabaseService;
import org.db.Fields;
import org.io.ChangeInterface;

import java.io.IOException;
import java.nio.file.*;

/**
 * Delete a file or folder locally when deletion happens on Google drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DeleteService implements ChangeInterface {

    private final DatabaseService dbService;
    private Change change;

    @Inject
    public DeleteService(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public final void execute() throws IOException{
        Vertex vertex = dbService.getVertex(change.getFileId());

        String absolutePath = vertex.getProperty(Fields.PATH);

        Path path = FileSystems.getDefault().getPath(absolutePath);

        Files.delete(path);
    }
}
