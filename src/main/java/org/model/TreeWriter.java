package org.model;

import com.google.api.services.drive.Drive;
import org.model.types.MimeType;

import javax.swing.*;
import java.io.*;
import java.util.List;

/**
 * Tree writer.
 *
 * Write a tree of file
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeWriter {

    private TreeNode root;
    private Drive driveService;

    public TreeWriter(TreeNode root, Drive service) {
        this.root = root;
        this.driveService = service;
    }

    public void write() throws FileNotFoundException, IOException {
        writeTree(root);
    }

    /**
     * Debug code
     *
     * @param node
     */
    public void writeTree(TreeNode node) throws FileNotFoundException, IOException, NullPointerException {
        File file        = new File(node.getAbsolutePath());
        int r, numberOfBytesCopied = 0;
        if (node.isFolder()) {
            file.mkdir();
        } else if(MimeType.all().contains(node.getData().getMimeType())){
            File ouputFile = new File(node.getAbsolutePath());
            PrintWriter writer = new PrintWriter(ouputFile);
            writer.write("Data to open file in on drive website");
            writer.close();
        } else {
            FileOutputStream fos      = new FileOutputStream(node.getAbsolutePath());
            InputStream inputStream   = this.downloadFile(driveService, node.getData());
            OutputStream outputStream = new FileOutputStream(file);

                while ((r = inputStream.read()) != -1) {
                    outputStream.write((byte) r);
                    numberOfBytesCopied++;
                }

                inputStream.close();
                outputStream.close();

        }

        List<TreeNode> children = node.getChildren();

        if (!children.isEmpty()) {
            for (TreeNode child : children) {
                writeTree(child);
            }
        }
    }



    private InputStream downloadFile(Drive service, com.google.api.services.drive.model.File file) throws IOException {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                // uses alt=media query parameter to request content
                return service.files().get(file.getId()).executeMediaAsInputStream();
            } catch (IOException e) {
                // An error occurred.
                System.out.println("error");
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }
}
