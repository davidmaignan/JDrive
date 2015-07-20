package org.writer;

import org.model.TreeNode;

import java.io.File;
import java.io.IOException;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements WriterInterface {
    private TreeNode node;
    private File file;

    @Override
    public boolean write(TreeNode node) {
        this.node = node;
        file = new File(node.getAbsolutePath());

        return file.mkdir();
    }
}
