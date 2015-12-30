package org.io.change.move;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.repository.DatabaseService;
import org.io.ChangeInterface;
import org.io.annotation.Document;
import org.io.change.writer.WriterChangeInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class MoveDocumentService implements ChangeInterface{

    private WriterChangeInterface writer;
    private DatabaseService dbService;
    private Change change;
    private final Logger logger;

    @Inject
    public MoveDocumentService(@Document WriterChangeInterface writer, DatabaseService dbService) {
        this.writer = writer;
        this.dbService = dbService;
        logger = LoggerFactory.getLogger(this.getClass().getSimpleName());

    }

    @Override
    public boolean execute() {
        try{
            if(writer.write(change)) {
                dbService.update(change);
            }

            return true;
        } catch (Exception exception) {
            logger.error("Failed to update db: " + exception);
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
