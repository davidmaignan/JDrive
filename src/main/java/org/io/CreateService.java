package org.io;

import com.google.api.services.drive.model.Change;
import database.repository.DatabaseService;
import org.writer.WriterInterface;

import java.io.IOException;

/**
 * CreateService: create a file from a change event
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class CreateService implements ChangeInterface {

    private Change change;
    private WriterInterface writer;
    private DatabaseService dbService;

    @Override
    public boolean execute() throws IOException {


        return true;
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public Change getChange() {
        return null;
    }
}
