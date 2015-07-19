package org.writer;

import org.model.TreeNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class DocumentWriter implements WriterInterface {
    private TreeNode node;
    private File file;
    private PrintWriter output;

    /**
     * Constructor 1 args
     *
     * @param node
     */
    public DocumentWriter(TreeNode node) throws FileNotFoundException {
        this.node = node;
        file      = new File(node.getAbsolutePath());
        output    = new PrintWriter(file);
    }

    @Override
    public boolean write() {
        output.write("File info to open in GDrive");
        output.close();

        return false;
    }
}
