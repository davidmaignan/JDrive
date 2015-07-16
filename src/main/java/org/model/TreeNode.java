package org.model;

import java.util.*;
import java.util.function.Consumer;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;

/**
 *  Tree structure to represent the Drive file structure
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-15.
 */
public class TreeNode implements Iterable<TreeNode>{

    private final String FOLDER_STRING = "application/vnd.google-apps.folder";
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

    /**
     * No args constructor
     */
    public TreeNode(){
        super();
        this.title               = "root";
        this.isFolder            = true;
        this.isSuperRoot         = true;
        this.isRoot              = false;
        this.isAuthenticatedUser = true;
        this.children            = new ArrayList<>();
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

        if ( ! file.getMimeType().equals(FOLDER_STRING)) {
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

    @Override
    public String toString() {
        return String.format(
                "TreeNode[ title: %10s, isFolder: %5b, parent %10s, children: %d\n",
                this.title,
                this.isFolder,
                (this.parent != null) ? this.parent.getTitle() : null,
                this.children.size());
    }

    @Override
    public Iterator<TreeNode> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super TreeNode> action) {
    }

    @Override
    public Spliterator<TreeNode> spliterator() {
        return null;
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
