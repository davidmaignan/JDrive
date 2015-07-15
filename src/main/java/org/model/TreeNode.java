package org.model;

import java.util.*;
import java.util.function.Consumer;
import com.google.api.services.drive.model.File;

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

    public TreeNode(){
        super();
        this.title       = "root";
        this.isFolder    = true;
        this.isSuperRoot = true;
        this.isRoot      = false;
        children         = new ArrayList<>();
    }

    public TreeNode(File file) {
        this.data        = file;
        this.id          = file.getId();
        this.parentId    = file.getParents().get(0).getId();
        this.isFolder    = true;
        this.isRoot      = false;
        this.isSuperRoot = false;
        this.title       = this.data.getTitle();
        children         = new ArrayList<>();

        if ( ! file.getMimeType().equals(FOLDER_STRING)) {
            isFolder = false;
        }

        if(file.getParents().get(0).getIsRoot()) {
            this.isRoot = true;
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

    @Override
    public String toString() {
        return String.format(
                "TreeNode[ title: %10s, isFolder: %5b, parent %10s, children: %d\n",
                this.title,
                this.isFolder,
                (this.parent != null) ? this.parent.getTitle() : null,
                this.children.size());
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
}
