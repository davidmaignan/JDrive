package drive.change.model;

import com.google.api.services.drive.model.Change;
import model.types.MimeType;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Structure containing the data from a change
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-04.
 */
public class CustomChange implements Comparable<CustomChange>{

    private static Logger logger = LoggerFactory.getLogger(CustomChange.class.getSimpleName());
    private Node changeNode;
    private Node fileNode;
    private Node newParentNode;
    private Node oldParentNode;
    private Change change;
    private String oldName;
    private String newName;
    private Boolean deleted = false;
    private Boolean trashed = false;
    private Boolean isNew = false;
    private Boolean isUnTrashed = false;
    public ChangeTypes type;
    private Long depth;

    public void setType(ChangeTypes type){
        this.type = type;
    }

    public ChangeTypes getType(){
        if(change == null){
            return ChangeTypes.NULL;
        } else if (isNew) {
            if(change.getFile().getMimeType().equals(MimeType.FOLDER)) {
                return ChangeTypes.NEW_FOLDER;
            } else {
                return ChangeTypes.NEW_FILE;
            }
        } else if(deleted){
            return ChangeTypes.DELETE;
        } else if (trashed){
            return ChangeTypes.TRASHED;
        } else if(isUnTrashed){
            return ChangeTypes.UNTRASHED;
        } else if(isDifferentParent()){
            return ChangeTypes.MOVE;
        } else if (isDifferentName()) {
            return ChangeTypes.MOVE;
        } else if(change.getFile().getMimeType().equals(MimeType.FOLDER)){
            return ChangeTypes.FOLDER_UPDATE;
        } else if (MimeType.all().contains(change.getFile().getMimeType())) {
            return ChangeTypes.DOCUMENT;
        } else {
            return ChangeTypes.FILE_UPDATE;
        }
    }

    public Long getDepth() {
        return depth;
    }

    public void setDepth(Long depth) {
        this.depth = depth;
    }

    private boolean isDifferentParent(){
        return ! this.oldParentNode.equals(newParentNode);
    }

    private boolean isDifferentName(){
        return ! this.oldName.equals(newName);
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }

    public Boolean getUnTrashed() {
        return isUnTrashed;
    }

    public void setUnTrashed(Boolean untrashed) {
        isUnTrashed = untrashed;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomChange that = (CustomChange) o;

        return type == that.type && depth == that.depth;
    }

    private int convertTypeToInt(){
        if(this.type == null)
            this.type = getType();

        switch (this.type){
            case NEW_FOLDER:
                return 0;
            case NEW_FILE:
                return 1;
            case UNTRASHED:
                return 2;
            case FILE_UPDATE:
            case FOLDER_UPDATE:
            case DOCUMENT:
                return 3;
            case MOVE:
                return 4;
            case TRASHED:
            case DELETE:
                return 5;
            default:
                return 6;
        }
    }

    @Override
    public int hashCode() {
        return 31 * getType().hashCode();
    }

    @Override
    public String toString() {
        return "CustomChange{" +
                "oldName='" + oldName + '\'' +
                ", newName='" + newName + '\'' +
                ", oldParentNode=" + oldParentNode +
                ", newParentNode=" + newParentNode +
                ", type=" + type +
                ", depth=" + depth +
                '}';
    }

    @Override
    public int compareTo(CustomChange o) {
        int convertTypeDiff = convertTypeToInt() - ((CustomChange) o).convertTypeToInt();


        if(convertTypeDiff != 0 || depth == null || o.depth == null) {
            return convertTypeDiff;
        } else {
//            int result = getDepth().compareTo(o.getDepth());
//            logger.debug(result + "");
            return getDepth().compareTo(o.getDepth());
        }
    }
}
