package drive.change.services;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-04.
 */
public class ConverterService {
    private FileRepository fileRepository;
    private Node fileNode;
    private Change change;

    private static Logger logger = LoggerFactory.getLogger(ConverterService.class);

    @Inject
    public ConverterService(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public CustomChange execute(Change change){

        CustomChange customChange = new CustomChange();

        customChange.setChange(change);

        fileNode = fileRepository.getNodeById(change.getFileId());
        customChange.setFileNode(fileNode);

        //Case new producer
        if(fileNode == null){
            customChange.setNew(true);
            Node parentNode = fileRepository.getNodeById(change.getFile().getParents().get(0));
            customChange.setNewParentNode(parentNode);
            customChange.setOldParentNode(parentNode);

            customChange.setOldName(change.getFile().getName());
            customChange.setNewName(change.getFile().getName());

            return customChange;
        }

        //Case deletion
        customChange.setDeleted(change.getRemoved());

        if(change.getRemoved()) {
            Node parentNode = fileRepository.getParent(change.getFileId());
            customChange.setNewParentNode(parentNode);
            customChange.setOldParentNode(parentNode);
            String name = fileRepository.getName(fileNode);
            customChange.setOldName(name);
            customChange.setNewName(name);
            return customChange;
        }

        //Case trashed
        if (getTrashed(change)){
            customChange.setTrashed(true);
        }

        //Case untrashed
        if(fileRepository.getTrashed(fileNode) && ! getTrashed(change)) {
            customChange.setUnTrashed(true);
        }

        Node newParentNode = fileRepository.getNodeById(change.getFile().getParents().get(0));
        Node oldParentNode = fileRepository.getParent(change.getFileId());

        customChange.setNewParentNode(newParentNode);
        customChange.setOldParentNode(oldParentNode);

        customChange.setOldName(fileRepository.getName(fileNode));
        customChange.setNewName(change.getFile().getName());

        return customChange;
    }

    /**
     * Get trashed label value if available
     *
     * @param change Change
     *
     * @return boolean
     */
    public boolean getTrashed(Change change) {
        return (change.getFile() != null
                && change.getFile().getExplicitlyTrashed() != null
                && change.getFile().getExplicitlyTrashed())
                || (change.getFile() != null
                && change.getFile().getTrashed() != null
                && change.getFile().getTrashed());
    }
}
