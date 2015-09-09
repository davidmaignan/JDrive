package org.io.change.create;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.io.ChangeInterface;
import org.io.annotation.Document;
import org.io.change.writer.WriterChangeInterface;

import java.io.IOException;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class CreateDocumentService implements ChangeInterface{

    private WriterChangeInterface writer;
    private DatabaseService dbService;
    private Change change;

    @Inject
    public CreateDocumentService(@Document WriterChangeInterface writer, DatabaseService dbService) {
        this.writer = writer;
        this.dbService = dbService;
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
