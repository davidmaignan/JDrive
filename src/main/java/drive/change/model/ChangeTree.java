package drive.change.model;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import database.DatabaseException;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.api.FileService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class ChangeTree {
    private static Logger logger = LoggerFactory.getLogger(ChangeTree.class);

    private final ChangeRepository changeRepository;
    private final FileRepository fileRepository;

    @Inject
    public ChangeTree(FileRepository fileRepository, ChangeRepository changeRepository) {
        this.changeRepository = changeRepository;
        this.fileRepository = fileRepository;
    }

    public void execute(List<ValidChange> list) throws Exception{
        List<Node> trashedNode = fileRepository.getTrashedList();

        while( ! list.isEmpty()){
            ValidChange validChange = list.remove(0);

            boolean result = true;

            if(validChange.isNewFile()){
                result = createNode(validChange.getChange());
            }

            // @todo investiqge if this condition is necessary
            if(validChange.isTrashed()) {
                trashedNode = fileRepository.getTrashedList();
            }

            if(result){
                //If trashed files get untrashed and is also moved
                //It must be recreated in its previous location to be moved after
                if(trashedNode.contains(validChange.getFileNode())){
                    fileRepository.markAsUnTrashed(validChange.getFileNode());
                }

                changeRepository.addChange(validChange.getChange());
            }
        }
    }

    private boolean createNode(Change change) throws Exception{
        try {
            String parentId = change.getFile().getParents().get(0).getId();

            Node parentNode = fileRepository.getNodeById(parentId);

            if (parentNode == null) {
                logger.error(change.toPrettyString());
                throw new Exception("Cannot create a node if parent does not exists");
            }

            Node newNode = fileRepository.createNode(change.getFile());

            return fileRepository.createParentRelation(newNode, parentNode);
        }catch (Exception exception){
            logger.error(change.toPrettyString());
            logger.error(exception.getMessage(), exception);
            return false;
        }
    }
}
