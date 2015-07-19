package org.writer;

import org.model.TreeNode;

import java.io.File;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements WriterInterface {
    private TreeNode node;
    private File file;

    /**
     * Constructor 1 args
     *
     * @param node
     */
    public FolderWriter(TreeNode node){
        this.node = node;
        file = new File(node.getAbsolutePath());
    }

    @Override
    public boolean write() {
        return file.mkdir();
    }
}
