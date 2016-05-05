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
        boolean deleted = changeRepository.getTrashed(change);
        changeStruct.setDeleted(deleted);

        if(deleted) {
            return changeStruct;
        }

        //Check if moved
        String path = fileRepository.getNodeAbsolutePath(fileNode);

        String newParent = change.getFile().getParents().get(0).getId();
        String oldParent = fileRepository.getParent(change.getFileId()).toString();

        logger.debug("Path: " + path);
        logger.debug("newParent: " + newParent);
        logger.debug("oldParent: " + oldParent);



        //Check if new content


        return changeStruct;
    }
}
