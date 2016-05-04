package drive;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import org.api.FileService;
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

    public boolean execute(List<Change> changeList) throws Exception{
        while( ! changeList.isEmpty()) {
            Change change = changeList.remove(0);
            Node node = fileRepository.getNodeById(change.getFileId());
            boolean result = true;

            if (node == null) {
                result = createNode(change);
            }

            if (result) {
                result = changeRepository.addChange(change);
            } else {
                result = changeRepository.createLonelyChange(change);
            }

            if ( ! result) {
                logger.error("Change: %d is not admissible\n", change.getId());
            }
        }

        return true;
    }

    private boolean createNode(Change change) throws Exception{
        String parentId = change.getFile().getParents().get(0).getId();

        Node newNode = fileRepository.createNode(change.getFile());

        Node parentNode = fileRepository.getNodeById(parentId);

        if (parentNode == null) {
            parentNode = createParentNode(parentId);
        }

        return fileRepository.createParentRelation(newNode, parentNode);
    }

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
            throw new Exception("Cannot create relation between " + node + " : " + parentNode);
        }

        return node;
    }
}
