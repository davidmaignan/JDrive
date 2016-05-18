package drive.change.services;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.api.change.ChangeService;
import drive.change.model.CustomChange;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by david on 2016-05-04.
 */
public class ConverterService {

    private FileRepository fileRepository;
    private ChangeRepository changeRepository;
    private ChangeService changeService;
    private Node fileNode;
    private Change change;

    private CustomChange customChange;

    private static Logger logger = LoggerFactory.getLogger(ConverterService.class);

    @Inject
    public ConverterService(FileRepository fileRepository, ChangeRepository changeRepository, ChangeService changeService){
        this.fileRepository = fileRepository;
        this.changeRepository = changeRepository;
        this.changeService = changeService;

        customChange = new CustomChange();
    }

    public CustomChange execute(Node changeNode){
        customChange.setChangeNode(changeNode);

        String changeId = changeRepository.getId(changeNode);

        change = changeService.get(changeId);
        customChange.setChange(change);

        if(change == null){
            return customChange;
        }

        fileNode = fileRepository.getNodeById(change.getFileId());
        customChange.setFileNode(fileNode);

        if(fileNode == null){
            return customChange;
        }

        //Check if deleted
        customChange.setDeleted(change.getDeleted());



        if(change.getDeleted()) {
            Node parentNode = fileRepository.getParent(change.getFileId());
            customChange.setNewParentNode(parentNode);
            customChange.setOldParentNode(parentNode);
            String title = fileRepository.getTitle(fileNode);
            customChange.setOldName(title);
            customChange.setNewName(title);
            return customChange;
        }

        customChange.setTrashed(getTrashed(change));

        Node newParentNode = fileRepository.getNodeById(change.getFile().getParents().get(0).getId());
        Node oldParentNode = fileRepository.getParent(change.getFileId());

        customChange.setNewParentNode(newParentNode);
        customChange.setOldParentNode(oldParentNode);

        customChange.setOldName(fileRepository.getTitle(fileNode));
        customChange.setNewName(change.getFile().getTitle());

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
