package org.io.change.writer;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.Fields;
import org.db.neo4j.DatabaseService;
import org.neo4j.graphdb.Node;
import org.writer.WriterInterface;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-28.
 */
public class MoveFolderWriter implements WriterChangeInterface {
    private final DatabaseService dbService;

    @Inject
    public MoveFolderWriter(DatabaseService dbService){
        this.dbService = dbService;
    }

    @Override
    public boolean write(Change change) {

        System.out.println("here");
        System.exit(0);

        String oldPathString = dbService.getNodePropertyById(change.getFileId(), Fields.PATH);
        String newPathString = String.format("%s/%s",
                dbService.getNodePropertyById(change.getFile().getParents().get(0).getId(), Fields.PATH),
                change.getFile().getTitle()
        );

        Path oldPath = FileSystems.getDefault().getPath(oldPathString);
        Path newPath = FileSystems.getDefault().getPath(newPathString);

        try{
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            exception.printStackTrace();
            //@todo log
            return false;
        }

        return true;
    }
}
