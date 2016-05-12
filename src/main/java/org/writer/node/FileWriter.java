package org.writer.node;

import com.google.inject.Inject;
import model.tree.TreeNode;
import org.api.DriveService;
import org.writer.WriterInterface;

import java.io.*;

/**
 * Write a file from a treeNode
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileWriter implements WriterInterface {
    private TreeNode node;
    private final DriveService driveService;

    @Inject
    public FileWriter(DriveService driveService) {
        this.driveService = driveService;
    }

    public void setNode(TreeNode node){
        this.node = node;
    }

    @Override
    public boolean write() {

        System.out.println(node);


        try {
            FileOutputStream fos      = new FileOutputStream(node.getAbsolutePath());
            InputStream inputStream   = this.downloadFile(driveService, node.getData());
            OutputStream outputStream = new FileOutputStream(node.getAbsolutePath());

            if (inputStream == null) {
                return false;
            }

            int numberOfBytesCopied = 0;
            int r;

            while ((r = inputStream.read()) != -1) {
                outputStream.write((byte) r);
                numberOfBytesCopied++;
            }

            inputStream.close();
            outputStream.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean write(String path, com.google.api.services.drive.model.File file) {
        try {
            FileOutputStream fos      = new FileOutputStream(path);
            InputStream inputStream   = this.downloadFile(driveService, file);
            OutputStream outputStream = new FileOutputStream(path);

            if (inputStream == null) {
                return false;
            }

            int numberOfBytesCopied = 0;
            int r;

            while ((r = inputStream.read()) != -1) {
                outputStream.write((byte) r);
                numberOfBytesCopied++;
            }

            inputStream.close();
            outputStream.close();

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    private InputStream downloadFile(DriveService service, com.google.api.services.drive.model.File file) throws IOException {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                return driveService.getDrive().files().get(file.getId()).executeMediaAsInputStream();
            } catch (IOException e) {
                // An error occurred.
                System.out.println("error");
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on DriveMimeTypes.
            return null;
        }
    }
}
