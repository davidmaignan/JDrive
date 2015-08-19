package org.model.tree;

import java.util.*;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import org.model.types.MimeType;

/**
 *  Tree structure to represent the Drive file structure
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-15.
 */

public class TreeNode {
    private File data;
    private String id;
    private String parentId;
    private TreeNode parent;
    private List<TreeNode> children;
    private String title;
    private boolean isFolder;
    private boolean isRoot;
    private boolean isSuperRoot;
    private boolean isAuthenticatedUser;
    private DateTime createdDate;
    private DateTime modifiedDate;
    private boolean isTrashed;

    /**
     * No args constructor
     */
    public TreeNode(String rootFolder){
        super();
        this.title               = rootFolder;
        this.isFolder            = true;
        this.isSuperRoot         = true;
        this.isRoot              = false;
        this.isAuthenticatedUser = true;
        this.children            = new ArrayList<>();
        this.data                = new File();
        this.isTrashed           = false;
        data.setMimeType(MimeType.FOLDER);
    }

    /**
     * Constructor with file argument
     *
     * @param file
     */
    public TreeNode(File file) {
        this.data                = file;
        this.id                  = file.getId();
        this.parentId            = this.getParentReferenceId();
        this.isFolder            = true;
        this.isRoot              = false;
        this.isSuperRoot         = false;
        this.title               = this.data.getTitle();
        this.isAuthenticatedUser = this.getOwnerShip();
        this.children            = new ArrayList<>();
        this.modifiedDate        = this.data.getModifiedDate();
        this.createdDate         = this.data.getCreatedDate();
        this.isTrashed           = false;

        if (file.getLabels().size() > 0) {
            this.isTrashed = file.getLabels().getTrashed().booleanValue();
        }

        if ( ! file.getMimeType().equals(MimeType.FOLDER)) {
            isFolder = false;
        }

        if(this.parentId != null) {
            this.isRoot = this.getParentReference().getIsRoot();
        }
    }

    /**
     * Add child
     *
     * @param file
     */
    public void addChild(File file) {
        TreeNode node = new TreeNode(file);
        node.setParent(this);
        this.setId(node.getParentId());
        children.add(new TreeNode(file));
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
        if(! node.isSuperRoot()) {
            getAbsolutePath(node.getParent(), path);
        }

        path.append("/");
        path.append(node.getTitle());

        return path;
    }

    /**
     * Get children of the node
     * @return
     */
    public List<TreeNode> getChildren() {
        return children;
    }

    public File getData() {
        return data;
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

    public String getTitle() {
        return title;
    }

    public boolean isRoot() {
        return isRoot;
    }

    public boolean isSuperRoot() {
        return isSuperRoot;
    }

    public boolean isAuthenticatedUser() {
        return isAuthenticatedUser;
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
        return data.getMimeType();
    }

    public DateTime getCreatedDate() {
        return createdDate;
    }

    public DateTime getModifiedDate() {
        return modifiedDate;
    }

    @Override
    public String toString() {
        return String.format(
                "TreeNode[ title: %10s, id: %25s isFolder: %5b, parent %10s, children: %d\n",
                this.title,
                this.id,
                this.isFolder,
                (this.parent != null) ? this.parent.getTitle() : null,
                this.children.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode treeNode = (TreeNode) o;

        if (!id.equals(treeNode.id)) return false;
        if (!parentId.equals(treeNode.parentId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + parentId.hashCode();
        return result;
    }

    /**
     * Get first parent reference
     *
     * @return ParentReference
     */
    private ParentReference getParentReference() {
        if (data.getParents().size() > 0) {
            return data.getParents().get(0);
        }

        return null;
    }

    /**
     * Get first parent id
     *
     * @return String
     */
    private String getParentReferenceId(){
        if(this.getParentReference() != null) {
            return this.getParentReference().getId();
        }

        return null;
    }

    /**
     * Get owner of the file
     *
     * @return User
     */
    private User getOwner(){
        if(data.getOwners().size() > 0) {
            return data.getOwners().get(0);
        }

        return null;
    }

    /**
     * Get if file is own by authenticated user
     *
     * @return boolean
     */
    private boolean getOwnerShip(){
        if (this.getOwner() != null) {
            return this.getOwner().getIsAuthenticatedUser();
        }

        return false;
    }
}
