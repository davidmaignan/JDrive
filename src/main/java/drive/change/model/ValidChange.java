package drive.change.model;

import com.google.api.services.drive.model.Change;
import database.repository.FileRepository;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drive api return different format of response for change.
 *
 * ValidChange checks that the change response is valid for:
 *
 *  - case 1: most cases (update, new file, trashed file ...) => change.getFile() must provide a File object
 *  - case 2: change.getFile() return null => it's a deletion so change.getDeleted() == true
 *
 *  NB: If a case is not asset, it is not valid and then disregarded.
 *
 */
public class ValidChange{
    private static Logger logger = LoggerFactory.getLogger(ValidChange.class);

    private Change change;
    private FileRepository fileRepository;
    private Node fileNode;

    public ValidChange(FileRepository fileRepository){
        this.fileRepository = fileRepository;
    }

    public Change getChange() {
        return change;
    }

    public void execute(Change change){
        this.change = change;

        fileNode = fileRepository.getNodeById(change.getFileId());
    }

    public boolean isValid(){
        if(change.getRemoved()){
            return fileNode != null;
        }

        return change.getFile() != null;
    }

    public Node getFileNode(){
        return fileNode;
    }

    public void setFileNode(Node fileNode){
        this.fileNode = fileNode;
    }

    /**
     * Check if it's a new file.
     *
     * NB: NewFile files marked as trashed are ignored.
     *
     * @return
     */
    public boolean isNewFile(){
        return change.getFile() != null && fileNode == null;
    }
}
