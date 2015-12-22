package org.io.change.writer;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.Fields;
import database.neo4j.DatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-28.
 */
public class MoveDocumentWriter implements WriterChangeInterface {
    private final DatabaseService dbService;
    private final Logger logger;

    @Inject
    public MoveDocumentWriter(DatabaseService dbService){
        this.dbService = dbService;
        logger = LoggerFactory.getLogger(this.getClass().getSimpleName());
    }

    @Override
    public boolean write(Change change) {
        String oldPathString = dbService.getNodePropertyById(change.getFileId(), Fields.PATH);
        String newPathString = String.format("%s/%s",
                dbService.getNodePropertyById(change.getFile().getParents().get(0).getId(), Fields.PATH),
                change.getFile().getTitle()
        );

        Path oldPath = FileSystems.getDefault().getPath(oldPathString);
        Path newPath = FileSystems.getDefault().getPath(newPathString);

        logger.info(oldPathString + " : " + newPathString);

        try{
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            logger.error("Fail to move the document: " + exception);
            return false;
        }

        return true;
    }
}
