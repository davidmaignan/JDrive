package org.io.change.move;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.io.ChangeInterface;
import org.io.annotation.Folder;
import org.io.change.writer.WriterChangeInterface;

/**
 * Service to create a new file from a change api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveFileService implements ChangeInterface{
    private DatabaseService dbService;
    private WriterChangeInterface writer;
    private Change change;

    @Inject
    public MoveFileService(@Folder WriterChangeInterface writer, DatabaseService dbService) {
        this.dbService = dbService;
        this.writer = writer;
    }

    @Override
    public boolean execute() {
        try{
            if(writer.write(change)) {
                dbService.save(change);
            } else {
                throw new Exception(String.format("Failed to create folder: %s", change.getFileId()));
            }

            return true;
        } catch (Exception exception) {
            //@todo implement sl4j
            return false;
        }
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public Change getChange(){
        return change;
    }
}
