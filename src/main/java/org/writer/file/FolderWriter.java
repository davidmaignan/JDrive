package org.writer.file;

import org.model.tree.TreeNode;
import org.writer.WriterInterface;

import java.io.File;

/**
 * Folder writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FolderWriter implements WriterInterface {

    private File file;

    public FolderWriter(com.google.api.services.drive.model.File file) {
        throw new UnsupportedOperationException(String.format("%s writeris not implemented yet", file.getMimeType()));
    }

    @Override
    public boolean write() {
//        file = new File(node.getAbsolutePath());
//
//        return file.mkdir();
        return false;
    }
}
