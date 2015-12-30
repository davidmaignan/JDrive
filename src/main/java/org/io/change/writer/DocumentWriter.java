package org.io.change.writer;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import org.api.FileService;
import database.Fields;
import database.repository.DatabaseService;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Google Document writer
 *
 * Create / Update a google document file with google drive content information
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DocumentWriter implements WriterChangeInterface {
    private final DatabaseService dbService;
    private final FileService fileService;
    private com.google.api.services.drive.model.File file;
    private PrintWriter output;
    private String parentPath;

    @Inject
    public DocumentWriter(DatabaseService dbService, FileService fileService) {
        this.dbService = dbService;
        this.fileService = fileService;
    }

    @Override
    public boolean write(Change change) {
        com.google.api.services.drive.model.File file = this.fileService.getFile(change.getFileId());

        try{
            File fileIO  = new File(this.getAbsolutePath(change));
            output       = new PrintWriter(fileIO);
            output.write(this.setContent(file));
            output.close();
        } catch (IOException e) {
            //@todo log
            return false;
        }

        return true;
    }

    /**
     * Set content for a google doc (doc, excel, presentation ...)
     * @param file File
     * @return String
     */
    private String setContent(com.google.api.services.drive.model.File file) {
        return String.format("" +
                "{\"url\": \"https://docs.google.com/open?id=%s\", " +
                "\"doc_id\": \"%s\", \"email\": " +
                "\"%s\", \"resource_id\": \"document:%s\"})",
                file.getId(),
                file.getId(),
                file.getOwners().get(0).getEmailAddress(),
                file.getId()
        );
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
                change.getFile().getTitle()
        );
    }
}
