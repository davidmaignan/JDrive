package model.tree;

import java.util.*;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import model.types.MimeType;

/**
 *  Tree structure to represent the DriveMimeTypes file structure
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-15.
 */

public class TreeNode {
    private String id;
    private String parentId;
    private TreeNode parent;
    private List<TreeNode> children;
    private String name;
    private boolean isFolder;
    private boolean isRoot;
    private DateTime createdDate;
    private DateTime modifiedDate;
    private boolean isTrashed;
    private long version;
    private String mimeType;

    /**
     * No args constructor
     */
    public TreeNode(){
        super();
        this.name     = "";
        this.isFolder = true;
        this.isRoot   = true;
        this.children = new ArrayList<>();
        this.mimeType = MimeType.FOLDER;
    }

    /**
     * Constructor with file argument
     *
     * @param file
     */
    public TreeNode(File file) {
        this.id                  = file.getId();
        this.parentId            = this.getParentId(file);
        this.isRoot              = false;
        this.name                = file.getName();
        this.children            = new ArrayList<>();
        this.modifiedDate        = file.getModifiedTime();
        this.createdDate         = file.getCreatedTime();
        this.isTrashed           = file.getTrashed();
        this.version             = file.getVersion();
        this.mimeType            = file.getMimeType();

        if ( ! file.getMimeType().equals(MimeType.FOLDER)) {
            isFolder = false;
        } else {
            this.isFolder = true;
        }
    }

    private String getParentId(File file){
        if(file.getParents() == null || file.getParents().isEmpty()){
            return null;
        }

        return file.getParents().get(0);
    }

    /**
     * Add child
     *
     * @param node
     */
    public void addChild(TreeNode node) {
        children.add(node);
        node.setParent(this);
    }

    public String getAbsolutePath(){
        StringBuilder path = new StringBuilder();

        return getAbsolutePath(this, path).toString().substring(1);
    }

    /**
     * Read recursively to root node and returned absolute path
     * @param node
     * @param path
     *
     * @return
     */
    private StringBuilder getAbsolutePath(TreeNode node, StringBuilder path) {
        if(! node.isRoot()) {
            getAbsolutePath(node.getParent(), path);
        }

        path.append("/");
        path.append(node.getName());

        return path;
    }

    /**
     * Get children of the node
     * @return
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public TreeNode getParent() {
        return parent;
    }

    private void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    public long getVersion() {
        return version;
    }

    public boolean isTrashed() {
        return isTrashed;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", parentId='" + parentId + '\'' +
                '}';
    }
}
