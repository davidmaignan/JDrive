package org.writer.node;

import org.model.tree.TreeNode;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class DocumentWriter implements NodeWriterInterface {
    private TreeNode node;
    private File file;
    private PrintWriter output;


    @Override
    public boolean write(TreeNode node) {
        try{
            this.node = node;
            file      = new File(node.getAbsolutePath());
            output    = new PrintWriter(file);
            output.write(this.setContent(node));
            output.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    private String setContent(TreeNode node) {
        return String.format("" +
                "{\"url\": \"https://docs.google.com/open?id=%s\", " +
                "\"doc_id\": \"%s\", \"email\": " +
                "\"%s\", \"resource_id\": \"document:%s\"})",
                node.getId(),
                node.getId(),
                node.getData().getOwners().get(0).getEmailAddress(),
                node.getId()
        );
    }
}
