package org.api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientElementIterable;
import org.db.neo4j.DatabaseService;
import org.db.Fields;
import org.io.ChangeInterface;
import org.io.DeleteService;
import org.io.MoveService;
import org.model.tree.TreeNode;
import org.model.types.MimeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.writer.FactoryProducer;
import org.writer.FileModule;

/**
 * Update service: apply changes received from Drive API
 *
 *  Change can 3 types:
 *    - hard deletion or Trashed label
 *    - change of content
 *    - change of location
 *    - both change of content and location
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
        logger = LoggerFactory.getLogger(this.getClass());
        this.dbService = dbService;
        this.fileService = fileService;
    }

    public ChangeInterface update(Change change) throws Exception {
//        boolean success = false;
//        ChangeInterface service;
//
//        Vertex vertex = dbService.getVertex(change.getFileId());
//
//        if (vertex != null) {
//            Long vertexDT = vertex.getProperty(Fields.MODIFIED_DATE);
//            Long changeDT = change.getModificationDate().getValue();
//
//            System.out.println(changeDT + " : " + vertexDT);
//
//            if (changeDT > vertexDT) {
//
//                //If file is deleted permanently or Trashed
//                if (change.getDeleted() || this.getTrashedValue(change)) {
//                    service = Guice.createInjector(new FileModule()).getInstance(DeleteService.class);
//                    service.setChange(change);
//                    return service;
//                }
//
//                //If file is not a folder: reload it's content
//                if ( ! change.getFile().getMimeType().equals(MimeType.FOLDER)) {
//                    File file = fileService.getFile(vertex.getProperty(Fields.ID).toString());
//
//                    success = FactoryProducer.getFactory("FILE").getWriter(vertex).write() || true;
//                }
//
//                //If file is moved
//                OrientElementIterable parentList = vertex.getProperty(Fields.PARENTS);
//                Vertex parentVertex              = (Vertex) parentList.iterator().next();
//
//                String oldParent = parentVertex.getProperty(Fields.ID);
//                String newParent = change.getFile().getParents().get(0).getId();
//
//                System.out.println(change.getFile().getTitle() + ": " + oldParent + " : " + newParent + " : " + change.getFile().getMimeType());
//
//                if (!oldParent.equals(newParent)) {
//                    service = Guice.createInjector(new FileModule()).getInstance(MoveService.class);
//                    service.setChange(change);
//                    return service;
//                }
//
//                //Update modifiedDate for folder update (either a file was moved in it or removed from it)
//                //but the folder itself was not changed only it's content
//                dbService.updateProperty(vertex, Fields.MODIFIED_DATE, changeDT);
//            }
//        }

        return null;
    }

    /**
     * Get trashed label value if available
     *
     * @param change Change
     * @return boolean
     */
    private boolean getTrashedValue(Change change) {
        return (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed().booleanValue());
    }
}
