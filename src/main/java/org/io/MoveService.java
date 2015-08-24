package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.db.Fields;

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
public class MoveService implements ChangeInterface {
    private final DatabaseService dbService;
    private Change change;

    @Inject
    public MoveService(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public void execute() throws IOException {
//        Vertex oldVertex = dbService.getVertex(change.getFileId());
//        Vertex newVertex = dbService.getVertex(change.getFile().getParents().get(0).getId());
//
//        String oldPathString = oldVertex.getProperty(Fields.PATH);
//        Path oldPath = FileSystems.getDefault().getPath(oldPathString);
//
//        String newPathString = newVertex.getProperty(Fields.PATH) + "/" + change.getFile().getTitle();
//        Path newPath = FileSystems.getDefault().getPath(newPathString);
//
//        System.out.println(oldPathString + " : " + newPathString);

//        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

//        this.update(newPathString);
    }

    /**
     * Update vertex and parent vertex
     * @param newPath String
     */
    private void update(String newPath) {
//        dbService.update(change.getFileId());
//        Vertex vertex = dbService.getVertex(change.getFileId());

//        vertex.setProperty(Fields.MODIFIED_DATE, change.getModificationDate().getValue());
//        vertex.setProperty(Fields.PATH, newPath);
//
//        OrientElementIterable parentList = vertex.getProperty(Fields.PARENTS);
//        Vertex parentVertex = (Vertex) parentList.iterator().next();
//
//        parentVertex.setProperty(Fields.ID, change.getFile().getParents().get(0).getId());
//        dbService.save(parentVertex);
//
//        dbService.save(vertex);
    }
}
