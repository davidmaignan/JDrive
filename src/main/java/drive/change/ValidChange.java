package drive.change;

import com.google.api.services.drive.model.Change;
import database.repository.FileRepository;
import org.neo4j.graphdb.Node;

/**
 * A valid change is a change that:
 *  1 - if not a new file it must exist in the database.
 *  2 - if new file newly created. (change.getFile() != null)
 */
public class ValidChange {
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
        if(change.getDeleted() && fileNode != null){
            return true;
        }

        if(isNewFile()) {
            return true;
        }

        return fileNode != null;
    }

    public Node getFileNode(){
        return fileNode;
    }

    /**
     * Check if it's a new file.
     *
     * NB: New files marked as trashed are ignored.
     *
     * @return
     */
    public boolean isNewFile(){
        return change.getFile() != null && fileNode == null && ! isTrashed();
    }

    public String getParentId(){
        return change.getFile().getParents().get(0).getId();
    }

    /**
     * Get trashed label value if available
     *
     * @return boolean
     */
    public boolean isTrashed() {
        boolean result = false;
        result =  (change.getFile() != null
                && change.getFile().getExplicitlyTrashed() != null
                && change.getFile().getExplicitlyTrashed())
                || (change.getFile() != null
                && change.getFile().getLabels() != null
                && change.getFile().getLabels().getTrashed());

        return result;
    }
}
