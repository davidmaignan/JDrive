package org.jdrive.file;

import org.model.TreeNode;

import java.io.File;

/**
 * File writer
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class Writer {

    private String filename;
    private File file;
    private TreeNode node;

    /**
     * Constructor 1 args
     *
     * @param node
     */
    public Writer(TreeNode node){
        this.node = node;

        file = new File(node.getTitle());
    }

    public boolean write() {
        if(node.isFolder() && ! file.exists()) {
            return file.mkdir();
        }

        return false;
    }

    public boolean overWrite(){
        if(node.isFolder()) {
            return file.mkdir();
        }

        return false;
    }

    public boolean exist(){
        return file.exists();
    }

    public boolean destroy() {
        return file.delete();
    }
}
