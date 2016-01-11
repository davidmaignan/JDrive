package org.writer.node;

import model.tree.TreeNode;
import org.writer.WriterInterface;

import java.io.File;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements WriterInterface {
    private TreeNode node;
    private File file;

    public FolderWriter(TreeNode node) {
        this.node = node;
    }

    @Override
    public boolean write() {
        file = new File(node.getAbsolutePath());

        return file.mkdir();
    }
}
