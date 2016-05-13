package drive.change.model;

import com.google.api.services.drive.model.Change;
import model.types.MimeType;
import org.neo4j.graphdb.Node;

/**
 * Structure containing the data from a change
 *
 * Created by david on 2016-05-04.
 */
public class CustomChange {
    private Node changeNode;
    private Node fileNode;
    private Node newParentNode;
    private Node oldParentNode;
    private Change change;
    private String oldName;
    private String newName;
    private Boolean deleted;
    private Boolean trashed;

    public ChangeTypes getType(){
        if(change == null){
            return ChangeTypes.NULL;
        } else if (fileNode == null) {
            return ChangeTypes.NULL;
        } else if(deleted != null && deleted){
            return ChangeTypes.DELETE;
        } else if (trashed != null && trashed){
            return ChangeTypes.TRASHED;
        } else if(! this.oldParentNode.equals(newParentNode)){
            return ChangeTypes.MOVE;
        } else if (!this.oldName.equals(newName)) {
            return ChangeTypes.MOVE;
        } else if(change.getFile().getMimeType().equals(MimeType.FOLDER)){
            return ChangeTypes.FOLDER_UPDATE;
        } else if (MimeType.all().contains(change.getFile().getMimeType())) {
            return ChangeTypes.DOCUMENT;
        } else {
            return ChangeTypes.FILE_UPDATE;
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

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public void setOldName(String oldName) {
        this.oldName = oldName;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Node getNewParentNode() {
        return newParentNode;
    }

    public Node getOldParentNode() {
        return oldParentNode;
    }

    public void setOldParentNode(Node oldParentNode) {
        this.oldParentNode = oldParentNode;
    }

    public void setNewParentNode(Node newParentNode) {
        this.newParentNode = newParentNode;
    }

    public Boolean getTrashed() {
        return trashed;
    }

    public void setTrashed(Boolean trashed) {
        this.trashed = trashed;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public Node getFileNode() {
        return fileNode;
    }

    public Node getChangeNode() {
        return changeNode;
    }

    @Override
    public String toString() {
        return "CustomChange{" +
                "oldParentNode=" + oldParentNode +
                ", newParentNode=" + newParentNode +
                ", oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                ", deleted=" + deleted +
                ", trashed=" + trashed +
                '}';
    }
}
