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
    private final FileService fileService;
    private final FileRepository fileRepository;

    @Inject
    public ChangeTree(FileRepository fileRepository, FileService fileService, ChangeRepository changeRepository) {
        this.changeRepository = changeRepository;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    public boolean execute(List<ValidChange> list) throws Exception{
        List<Node> trashedNode = fileRepository.getTrashedList();

        while( ! list.isEmpty()){
            ValidChange validChange = list.remove(0);

            boolean result = true;

            if(validChange.isNewFile()){
                result = createNode(validChange.getChange());
            }

            if(result){
                //If trashed files get untrashed and moved at the same time
                if(trashedNode.contains(validChange.getFileNode())){
                    fileRepository.markAsUnTrashed(validChange.getFileNode());
                }

                changeRepository.addChange(validChange.getChange());
            }
        }

        return true;
    }

    private boolean createNode(Change change) throws Exception{
        try {
            String parentId = change.getFile().getParents().get(0).getId();

            Node parentNode = fileRepository.getNodeById(parentId);

            if (parentNode == null) {
                logger.debug("This code should never be executed.");
                parentNode = createParentNode(parentId);
            }

            Node newNode = fileRepository.createNode(change.getFile());

            return fileRepository.createParentRelation(newNode, parentNode);
        }catch (Exception exception){
            logger.error(exception.getMessage(), exception);
            return true;
        }
    }

    /**
     * Create recursively the parent Node in case Google returns the changes unordered.
     *
     * @param fileId
     * @return
     * @throws Exception
     */
    private Node createParentNode(String fileId) throws Exception{
        File file = fileService.getFile(fileId);

        if (file == null) {
            throw new Exception("Error google api. No file found for id: " + fileId);
        }

        Node node = fileRepository.createNode(file);

        String parentId = file.getParents().get(0).getId();

        Node parentNode = fileRepository.getNodeById(parentId);

        if (parentNode == null) {
            parentNode = createParentNode(parentId);
        }

        boolean result = fileRepository.createParentRelation(node, parentNode);

        if ( ! result) {
            throw new DatabaseException("Cannot create relation between " + node + " : " + parentNode);
        }

        return node;
    }
}
