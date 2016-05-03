package io;

import com.google.api.services.drive.model.Change;
import database.Fields;
import org.io.ChangeInterface;
import org.io.ModifiedService;
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
    private String oldPathString;
    private String newPathString;

    public MoveService(String oldPathString, String newPathString){
        this.oldPathString = oldPathString;
        this.newPathString = newPathString;
    }

    @Override
    public boolean execute() throws IOException {
        Path oldPath = FileSystems.getDefault().getPath(oldPathString);
        Path newPath = FileSystems.getDefault().getPath(newPathString);

        System.out.println(oldPathString + " : " + newPathString);

        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);

        return true;
    }

    @Override
    public Change getChange() {
        return null;
    }
}
