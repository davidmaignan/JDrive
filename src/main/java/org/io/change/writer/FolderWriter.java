package org.io.change.writer;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.Fields;
import database.repository.DatabaseService;

import java.io.File;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements WriterChangeInterface {
    private File file;
    private Change change;
    private String parentPath;
    private DatabaseService dbService;

    @Inject
    public FolderWriter(DatabaseService dbService){
        this.dbService = dbService;
    }

    public FolderWriter() {}

    @Override
    public boolean write(Change change) {
        try {
            return new File(this.getAbsolutePath(change)).mkdir();
        } catch (Exception exception) {
            //@todo log
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Get file path
     *
     * @param change Change
     *
     * @return String
     */
    protected String getAbsolutePath(Change change) {
        return String.format(
                "%s/%s",
                dbService.getNodePropertyById(change.getFile().getParents().get(0).getId(), Fields.PATH),
                change.getFile().getTitle().toString()
        );
    }
}
