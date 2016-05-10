package drive.change;

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
    private Node changeNode;
    private Node fileNode;
    private Change change;
    private String fileName;
    private String path;
    private String newPath;

    private ChangeStruct changeStruct;

    private static Logger logger = LoggerFactory.getLogger(ChangeInterpreted.class);

    @Inject
    public ChangeInterpreted(FileRepository fileRepository, ChangeRepository changeRepository, ChangeService changeService){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
        this.changeService = changeService;

        changeStruct = new ChangeStruct();
    }

    public void setChange(Node changeNode){
        this.changeNode = changeNode;
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

        if(fileNode == null){
            return changeStruct;
        }

        Long changeVersion = changeRepository.getVersion(changeNode);
        Long fileVersion = fileRepository.getVersion(fileNode);

        changeStruct.setFileVersion(fileVersion);
        changeStruct.setChangeVersion(changeVersion);

        //Check if deleted
        changeStruct.setDeleted(change.getDeleted());
        changeStruct.setTrashed(getTrashed(change));

        String newParent = change.getFile().getParents().get(0).getId();
        Node newParentNode = fileRepository.getNodeById(newParent);
        Node oldParentNode = fileRepository.getParent(change.getFileId());

        changeStruct.setNewParentNode(newParentNode);

        logger.debug(oldParentNode + ":" +newParentNode);

        logger.debug(fileRepository.getFileId(oldParentNode) + ": " + newParent);

//        changeStruct.setOldParent(oldParent);
//        changeStruct.setNewParent(newParent);

        String oldParentPath = fileRepository.getNodeAbsolutePath(oldParentNode);

        changeStruct.setOldParentPath(oldParentPath);
        changeStruct.setNewParentPath(oldParentPath);

        if(! oldParentNode.equals(newParentNode)) {
            String newParentPath = fileRepository.getNodeAbsolutePath(newParentNode);
            changeStruct.setNewParentPath(newParentPath);
        }

        //Check if renamed
        String originalTitle = fileRepository.getTitle(fileNode);
        String newTitle = change.getFile().getTitle();

        changeStruct.setOldName(originalTitle);
        changeStruct.setNewName(newTitle);

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
        boolean result = false;
        result =  (change.getFile() != null
                && change.getFile().getExplicitlyTrashed() != null
                && change.getFile().getExplicitlyTrashed())
                || (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed());

        return result;
    }

    public boolean isDeleted(Change change){
        return change.getDeleted();
    }
}
