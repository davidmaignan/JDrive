package org.writer;

import org.model.tree.TreeNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class DocumentWriter implements WriterInterface {
    private TreeNode node;
    private File file;
    private PrintWriter output;


    @Override
    public boolean write(TreeNode node) {
        try{
            this.node = node;
            file      = new File(node.getAbsolutePath());
            output    = new PrintWriter(file);
            output.write("File info to open in GDrive");
            output.close();
        } catch (IOException e) {

            return false;
        }

        return true;
    }
}
