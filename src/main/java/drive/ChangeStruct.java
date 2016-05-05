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
    private String oldName;
    private String newName;
    private String oldParentPath;
    private String newParentPath;
    private Boolean deleted;
    private Long changeVersion;
    private Long fileVersion;
    private String oldParent;
    private String newParent;

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
        } else if(! this.oldParent.equals(newParent)){
            return ChangeTypes.MOVE;
        } else if (!this.oldName.equals(newName)) {
            return ChangeTypes.MOVE;
        } else {
            return ChangeTypes.UPDATE;
        }
    }

    public String getNewPath(){
        return String.format("%s/%s", this.getNewParentPath(), newName);
    }

    public String getOldPath(){
        return String.format("%s/%s", this.getOldParentPath(), oldName);
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

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setOldParentPath(String oldParentPath) {
        this.oldParentPath = oldParentPath;
    }

    public void setNewParentPath(String newParentPath) {
        this.newParentPath = newParentPath;
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

    public void setOldParent(String oldParent) {
        this.oldParent = oldParent;
    }

    public void setNewParent(String newParent) {
        this.newParent = newParent;
    }

    public Change getChange() {
        return change;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldParentPath() {
        return oldParentPath;
    }

    public String getNewParentPath() {
        return newParentPath;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Long getChangeVersion() {
        return changeVersion;
    }

    public Long getFileVersion() {
        return fileVersion;
    }

    public String getOldParent() {
        return oldParent;
    }

    public String getNewParent() {
        return newParent;
    }

    @Override
    public String toString() {
        return "ChangeStruct{" +
                "oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                ", oldParentPath='" + oldParentPath + '\'' +
                ", newParentPath='" + newParentPath + '\'' +
                ", deleted=" + deleted +
                ", changeVersion=" + changeVersion +
                ", fileVersion=" + fileVersion +
                ", oldParent='" + oldParent + '\'' +
                ", newParent='" + newParent + '\'' +
                '}';
    }
}
