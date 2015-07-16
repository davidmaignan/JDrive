package org.jdrive.file;

import org.model.TreeNode;

/**
 * JDrive File
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class JDriveFile {

    private TreeNode node;

    public JDriveFile(TreeNode node) {
        this.node = node;
    }

    public String getAbsolutePath(){
        StringBuilder path = new StringBuilder();

        return getAbsolutePath(node, path).toString();
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
}
