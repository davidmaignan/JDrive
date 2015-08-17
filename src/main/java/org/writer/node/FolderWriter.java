package org.writer.node;

import org.model.tree.TreeNode;

import java.io.File;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements NodeWriterInterface {
    private TreeNode node;
    private File file;

    @Override
    public boolean write(TreeNode node) {
        this.node = node;
        file = new File(node.getAbsolutePath());

        return file.mkdir();
    }
}