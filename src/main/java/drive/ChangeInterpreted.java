package drive;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import io.DeleteService;
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

        if(changeVersion.equals(fileVersion)){
            return changeStruct;
        }

        //Check if deleted
        boolean deleted = getTrashed(change);
        changeStruct.setDeleted(deleted);

        if(deleted) {
            return changeStruct;
        }

        String newParent = change.getFile().getParents().get(0).getId();
        String oldParent = fileRepository.getParent(change.getFileId()).toString();

        changeStruct.setOldParent(oldParent);
        changeStruct.setNewParent(newParent);

        String oldParentPath = fileRepository.getNodeAbsolutePath(oldParent);

        changeStruct.setOldParent(oldParent);
        changeStruct.setNewParent(newParent);
        changeStruct.setOldParentPath(oldParentPath);
        changeStruct.setNewParentPath(oldParentPath);

        if(! oldParent.equals(newParent)) {
            String newParentPath = fileRepository.getNodeAbsolutePath(newParent);
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
        return  change.getDeleted()
                || (change.getFile() != null
                && change.getFile().getExplicitlyTrashed() != null
                && change.getFile().getExplicitlyTrashed())
                || (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed());
    }
}
