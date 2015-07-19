package org.writer;

import com.google.api.services.drive.Drive;
import org.model.TreeNode;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class FileWriter implements WriterInterface {
    private TreeNode node;

    public FileWriter(TreeNode node) {
        this.node = node;
    }


    @Override
    public boolean write() {
//        FileOutputStream fos      = new FileOutputStream(node.getAbsolutePath());
//        InputStream inputStream   = this.downloadFile(driveService, node.getData());
//        OutputStream outputStream = new FileOutputStream(file);
//
//        int numberOfBytesCopied = 0;
//        int r;
//
//        try {
//            while ((r = inputStream.read()) != -1) {
//                outputStream.write((byte) r);
//                numberOfBytesCopied++;
//            }
//
//            inputStream.close();
//            outputStream.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return false;
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
