package org.api;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientElementIterable;
import org.db.DatabaseService;
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

import java.util.HashSet;

/**
 * Update service
 *
 * Update file locally after getting changes notification from Drive API
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

    public ChangeInterface update(Change change) throws Exception {
        boolean success = false;
        ChangeInterface service;

        Vertex vertex = dbService.getVertex(change.getFileId());

        if (vertex != null) {
            Long vertexDT = vertex.getProperty(Fields.MODIFIED_DATE);
            Long changeDT = change.getModificationDate().getValue();
            String absolutePath = vertex.getProperty(Fields.PATH);

            System.out.println(changeDT + " : " + vertexDT);

            /*
             Probleme: change can 3 types
              - change of content: just reload the content
              - change of location: need to find the new parent.
                - if found then move file to new location
                - if not found (parent is new as well - need to find recursively until we found a known parent)
              - change of both: need to determine parent then reload content
             */

            if(changeDT > vertexDT) {

                //System.out.println(change.getDeleted());

                if (change.getDeleted() || this.getTrashedValue(change)) {
                    service = Guice.createInjector(new FileModule()).getInstance(DeleteService.class);
                    service.setChange(change);
                    return service;
                }

//                //If not a folder - reload file content
//                if( ! change.getFile().getMimeType().equals(MimeType.FOLDER)) {
//                    File file = fileService.getFile(vertex.getProperty(Fields.ID).toString());
//
//                    boolean result = FactoryProducer.getFactory("FILE").getWriter(vertex).write() || true;
//
//                    if(result) {
//                        OrientElementIterable parentList = vertex.getProperty(Fields.PARENTS);
//
//                        Vertex parentVertex = (Vertex)parentList.iterator().next();
//
//                        String oldParent = parentVertex.getProperty(Fields.ID);
//                        String newParent = change.getFile().getParents().get(0).getId();
//
//                        if (! oldParent.equals(newParent)) {
//                            service = Guice.createInjector(new FileModule()).getInstance(MoveService.class);
//                            service.setChange(change);
//                            return service;
//                        }
//                    }
//                }
//                System.out.println(change.getFile().getParents().get(0).getId() + " - " + parentVertex.getProperty(Fields.ID));
            }
        }

        return null;
    }

    /**
     * Get trashed label value if available
     * @param change Change
     * @return boolean
     */
    private boolean getTrashedValue(Change change){
        return (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed().booleanValue());
    }
}
