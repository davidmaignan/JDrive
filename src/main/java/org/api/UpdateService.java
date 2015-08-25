package org.api;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Inject;
import org.db.neo4j.DatabaseService;
import org.db.Fields;
import org.io.ChangeInterface;
import org.io.DeleteService;
import org.model.tree.TreeNode;
import org.model.types.MimeType;
import org.neo4j.graphdb.Node;
import org.writer.FactoryProducer;
import org.writer.FileModule;

import java.util.List;

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
    private FileService fileService;
    private DatabaseService dbService;
    private ChangeInterface deleteService;
    private ChangeInterface moveService;
    private FactoryProducer factoryProducer;
    private TreeNode node;
    private List list;

    public UpdateService(List list){
        this.list = list;
    }

    @Inject
    public UpdateService(
            DatabaseService dbService,
            FileService fileService,
            ChangeInterface deleteService,
            ChangeInterface moveService,
            FactoryProducer factoryProducer
    ) {
        this.dbService = dbService;
        this.fileService = fileService;
        this.deleteService = deleteService;
        this.moveService = moveService;
        this.factoryProducer = factoryProducer;
    }



    public ChangeInterface update(Change change) throws Exception {
        boolean success = false;
        ChangeInterface service;

        Node node = dbService.getNodeById(change.getFileId());

        if (node != null) {
            Long vertexDT = Long.valueOf(node.getProperty(Fields.MODIFIED_DATE).toString());
            Long changeDT = change.getModificationDate().getValue();

//            System.out.println(changeDT + " : " + vertexDT);
//            System.out.println(changeDT > vertexDT);

            if (changeDT > vertexDT) {

                //If file is deleted permanently or Trashed
                if (this.getChangeDeleted(change) || this.getTrashedLabel(change)) {
                    deleteService.setChange(change);
                    return deleteService;
                }

//                //If file is not a folder: reload it's content
                if ( ! change.getFile().getMimeType().equals(MimeType.FOLDER)) {
                    File file = fileService.getFile(node.getProperty(Fields.ID).toString());

                    success = this.factoryProducer.getFactory("FILE").getWriter(node).write() || true;
                }
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
            }
        }

        return null;
    }

    private boolean getChangeDeleted(Change change) {
        return change.getDeleted() != null && change.getDeleted();
    }


    /**
     * Get trashed label value if available
     *
     * @param change Change
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

//        return (change.getFile() != null
//                && (change.getFile().getExplicitlyTrashed()
//                || (change.getFile().getLabels() != null && change.getFile().getLabels().getTrashed().booleanValue())));
    }
}
