package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.Fields;
import database.neo4j.DatabaseService;

import java.io.IOException;

/**
 * Service to update db node modified date on change api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ModifiedService implements ChangeInterface {

    protected Change change;
    protected DatabaseService dbService;

    public ModifiedService(){}

    @Inject
    public ModifiedService(DatabaseService dbService) {
        this.dbService = dbService;
    }

    @Override
    public void setChange(Change change) {
        this.change = change;
    }

    @Override
    public Change getChange() {
        return null;
    }

    @Override
    public boolean execute() throws IOException {
        return this.updateDB();
    }

    /**
     * Update modified date
     *
     * @return boolean
     */
    protected boolean updateDB(){
        try {
            dbService.update(change.getFileId(),
                    Fields.MODIFIED_DATE,
                    (String.valueOf(change.getFile().getModifiedDate().getValue()))
            );
        }catch (Exception exception) {
            //@todo log exception
            return false;
        }

        return true;
    }
}
