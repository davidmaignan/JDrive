package org.io.change.move;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.neo4j.DatabaseService;
import org.io.ChangeInterface;
import org.io.annotation.Folder;
import org.io.change.writer.WriterChangeInterface;

/**
 * JDrive
 * David Maignan <davidmaignan@gmail.com>
 */
public class MoveFolderService implements ChangeInterface{
    private DatabaseService dbService;
    private WriterChangeInterface writer;
    private Change change;

    @Inject
    public MoveFolderService(@Folder WriterChangeInterface writer, DatabaseService dbService) {
        this.writer    = writer;
        this.dbService = dbService;
    }

    @Override
    public boolean execute() {
        try{
            if(writer.write(change)) {
                dbService.save(change);
            } else {
                throw new Exception(String.format("Failed to create folder: %s", change.getFile().getTitle()));
            }

            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
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
