package drive.change.model;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import org.api.change.ChangeService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-04.
 */
public class ChangeInterpreted {

    private FileRepository fileRepository;
    private ChangeRepository changeRepository;
    private ChangeService changeService;
    private Node fileNode;
    private Change change;

    private ChangeStruct changeStruct;

    private static Logger logger = LoggerFactory.getLogger(ChangeInterpreted.class);

    @Inject
    public ChangeInterpreted(FileRepository fileRepository, ChangeRepository changeRepository, ChangeService changeService){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
        this.changeService = changeService;

        changeStruct = new ChangeStruct();
    }

    public ChangeStruct execute(Node changeNode){
        changeStruct.setChangeNode(changeNode);

        String changeId = changeRepository.getId(changeNode);

        change = changeService.get(changeId);
        changeStruct.setChange(change);

        if(change == null){
            return changeStruct;
        }

        fileNode = fileRepository.getFileNodeFromChange(changeNode);
        changeStruct.setFileNode(fileNode);

//        Long changeVersion = changeRepository.getVersion(changeNode);
//        Long fileVersion = fileRepository.getVersion(fileNode);
//        changeStruct.setFileVersion(fileVersion);
//        changeStruct.setChangeVersion(changeVersion);

        //Check if deleted
        changeStruct.setDeleted(change.getDeleted());

        if(change.getDeleted()) {
            Node parentNode = fileRepository.getParent(change.getFileId());
            changeStruct.setNewParentNode(parentNode);
            changeStruct.setOldParentNode(parentNode);
            String title = fileRepository.getTitle(fileNode);
            changeStruct.setOldName(title);
            changeStruct.setNewName(title);
            return changeStruct;
        }

        changeStruct.setTrashed(getTrashed(change));

        Node newParentNode = fileRepository.getNodeById(change.getFile().getParents().get(0).getId());
        Node oldParentNode = fileRepository.getParent(change.getFileId());

        changeStruct.setNewParentNode(newParentNode);
        changeStruct.setOldParentNode(oldParentNode);

        changeStruct.setOldName(fileRepository.getTitle(fileNode));
        changeStruct.setNewName(change.getFile().getTitle());

        return changeStruct;
    }

    /**
     * Get trashed label value if available
     *
     * @param change Change
     *
     * @return boolean
     */
    public boolean getTrashed(Change change) {
        boolean result;
        result =  (change.getFile() != null
                && change.getFile().getExplicitlyTrashed() != null
                && change.getFile().getExplicitlyTrashed())
                || (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed());

        return result;
    }
}
