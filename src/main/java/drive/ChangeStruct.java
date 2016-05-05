package drive;

import com.google.api.services.drive.model.Change;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structure containing the data from a change
 *
 * Created by david on 2016-05-04.
 */
public class ChangeStruct {
    private static Logger logger = LoggerFactory.getLogger(ChangeStruct.class);

    private Node changeNode;
    private Node fileNode;
    private Change change;
    private String fileName;
    private String path;
    private String newPath;
    private Boolean deleted;
    private Long changeVersion;
    private Long fileVersion;

    public ChangeStruct(){
    }

    public ChangeTypes getType(){
        if(change == null){
            return ChangeTypes.NULL;
        } else if (fileNode == null) {
            return ChangeTypes.NULL;
        } else if(deleted != null && deleted){
            return ChangeTypes.DELETE;
        } else if(changeVersion != null && changeVersion.equals(fileVersion) == true) {
            return ChangeTypes.VERSION;
        } else {
            return null;
        }
    }

    public void setChangeNode(Node changeNode) {
        this.changeNode = changeNode;
    }

    public void setFileNode(Node fileNode) {
        this.fileNode = fileNode;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNewPath(String newPath) {
        this.newPath = newPath;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public void setChangeVersion(Long changeVersion) {
        this.changeVersion = changeVersion;
    }

    public void setFileVersion(Long fileVersion) {
        this.fileVersion = fileVersion;
    }
}
