package org.api;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Vertex;
import org.db.DatabaseService;
import org.model.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update service
 *
 * Update file locally after recieving changes notification from Drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class UpdateService {
    private final FileService fileService;
    private final DatabaseService dbService;
    private final Logger logger;
    private TreeNode node;
    private Drive.Changes changeList;

    @Inject
    public UpdateService(DatabaseService dbService, FileService fileService) {
        logger           = LoggerFactory.getLogger(this.getClass());
        this.dbService   = dbService;
        this.fileService = fileService;
    }

    public void update(Change change){
        Vertex vertex = dbService.getVertex(change.getFileId());

        if (vertex != null) {
            Long vertexDT = ((DateTime)vertex.getProperty("modifiedDate")).getValue();
            Long changeDT = change.getModificationDate().getValue();
            String absolutePath = vertex.getProperty("AbsolutePath");

            if(absolutePath != null) {
                logger.warn(absolutePath.toString());
            }

//            logger.warn(vertexDT + ": " + changeDT);

            if(changeDT > vertexDT) {
                update(vertex);
            }
        }
    }

    private boolean update(Vertex vertex) {
        File file = fileService.getFile(vertex.getProperty("identifier").toString());

        if(file == null) {
            return false;
        }

        //logger.warn(dbService.getFullPath(vertex));

        return true;
    }

}
