package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.api.DriveService;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Write a file from a treeNode
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class File implements WriterInterface {
    private final DriveService driveService;
    private final FileRepository fileRepository;

    private String fileId;

    @Inject
    public File(DriveService driveService, FileRepository fileRepository) {
        this.driveService = driveService;
        this.fileRepository = fileRepository;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public boolean delete(String path) {
        java.io.File file = new java.io.File(path);

        return file.delete();
    }

    @Override
    public boolean write(String path) {
        try {
            FileOutputStream fos      = new FileOutputStream(path);
            InputStream inputStream   = this.downloadFile(driveService, fileId);
            OutputStream outputStream = new FileOutputStream(path);

            if (inputStream == null) {
                System.out.println("no inputStream");
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

    private InputStream downloadFile(DriveService service, String id) throws IOException {
        try {
            return driveService.getDrive().files().get(id).executeMediaAsInputStream();
        } catch (IOException e) {
            // An error occurred.
            System.out.println("error");
            e.printStackTrace();
            return null;
        }
    }
}
