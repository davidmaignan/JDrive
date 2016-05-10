package org.api;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import database.Fields;
import database.repository.DatabaseService;
import org.io.*;
import org.io.change.create.FactoryChangeCreateService;
import org.io.change.move.FactoryChangeMoveService;
import model.tree.TreeNode;
import org.neo4j.graphdb.Node;

import java.util.List;

/**
 * Update service: apply changes received from Drive API
 *
 *  Change can be different types:
 *    - hard deletion or Trashed label
 *    - change of content
 *    - change of location
 *    - both change of content and location
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class UpdateService {
    private FileService fileService;
    private DatabaseService dbService;
    private ChangeInterface deleteService;
    private ChangeInterface moveService;
    private ChangeInterface modifiedService;
    private ChangeInterface createService;
    private FactoryChangeCreateService factoryChangeCreateService;
    private FactoryChangeMoveService factoryChangeMoveService;
    private TreeNode node;
    private List list;

    @Inject
    public UpdateService(
            FactoryChangeCreateService factoryChangeCreateService,
            FactoryChangeMoveService factoryChangeMoveService,
            DatabaseService dbService) {
        this.factoryChangeCreateService = factoryChangeCreateService;
        this.factoryChangeMoveService = factoryChangeMoveService;
        this.dbService = dbService;
    }

    public ChangeInterface update(Change change) throws Exception {
        File file = change.getFile();

        Node node = dbService.getNodeById(file.getId());

        if (node != null) {

            Long vertexDT = Long.valueOf(dbService.getNodePropertyById(file.getId(), Fields.MODIFIED_DATE));
            Long changeDT = change.getModificationDate().getValue();
//
            System.out.println(changeDT + " : " + vertexDT);
            System.out.println(changeDT > vertexDT);
//
            if (changeDT > vertexDT) {

                System.out.println(change);

                //If title changed or parent change
                if ( ! file.getTitle().equals(dbService.getNodePropertyById(file.getId(), Fields.TITLE))) {

//                    System.out.println("title changed");
//                    System.exit(0);

                    return factoryChangeMoveService.get(change);
                }



                //If file -> reload content

                //Otherwise change db modified date
//
//                //If file is deleted permanently or Trashed
//                if (this.getChangeDeleted(change) || this.getTrashedLabel(change)) {
//                    deleteService.setChange(change);
//                    return deleteService;
//                }
//
////                //If file is not a folder: reload it's content
////                if ( ! change.getFile().getMimeType().equals(MimeType.FOLDER)) {
//////                    File file = fileService.getFile(node.getProperty(Fields.ID).toString());
//////                    success = this.factoryProducer.getFactory("FILE").getWriter(node).write() || true;
////                }
//
//                //This case is unlikely to happen. If it's the case - need to log this change and investigate
//                if ("true".equals(node.getProperty(Fields.IS_ROOT).toString())) {
//                    return null;
//                }
//
//                // If file/folder has been moved
//                if(change.getFile().getParents() != null && change.getFile().getParents().size() > 0) {
//
//                    Node oldParent = dbService.getParent(fileId);
//
//                    String oldParentId = oldParent.getProperty(Fields.ID).toString();
//                    String parentId    = change.getFile().getParents().get(0).getId();
//
//                    if( ! oldParentId.equals(parentId)) {
//                        moveService.setChange(change);
//                        return moveService;
//                    }
//                }
//
//                //If reach this part file / folder is in the same location. Just need to update the db
//                modifiedService.setChange(change);
//                return modifiedService;

                return null;
            }

            return null;
        }

        return factoryChangeCreateService.get(change);
    }

    private boolean getChangeDeleted(Change change) {
        return change.getDeleted() != null && change.getDeleted();
    }


    /**
     * Get trashed label value if available
     *
     * @param change Change
     *
     * @return boolean
     */
    private boolean getTrashedLabel(Change change) {
        return (change.getFile() != null
                    && change.getFile().getExplicitlyTrashed() != null
                    && change.getFile().getExplicitlyTrashed()
                )
                || (change.getFile() != null
                    && change.getFile().getLabels() != null
                    && change.getFile().getLabels().getTrashed()
                );
    }
}
