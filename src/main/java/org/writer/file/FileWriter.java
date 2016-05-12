package org.writer.file;

import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import org.api.DriveService;
import org.api.FileService;
import org.writer.WriterInterface;

import java.io.IOException;
import java.io.InputStream;

/**
 * Write a file from a vertex
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileWriter implements WriterInterface {
    private final DriveService driveService;
    private final FileService fileService;

    @Inject
    public FileWriter(DriveService driveService, FileService fileService) {
        this.driveService = driveService;
        this.fileService  = fileService;
    }

//    public void setVertex(Vertex vertex){
//        this.vertex = vertex;
//    }

    @Override
    public boolean write() {
//        try {
//            FileOutputStream fos      = new FileOutputStream(vertex.getProperty(Fields.PATH).toString());
//            InputStream inputStream   = this.downloadFile(driveService, this.getFile(vertex.getProperty(Fields.ID).toString()));
//            OutputStream outputStream = new FileOutputStream(vertex.getProperty(Fields.PATH).toString());
//
//            if (inputStream == null) {
//                return false;
//            }
//
//            int numberOfBytesCopied = 0;
//            int r;
//
//            while ((r = inputStream.read()) != -1) {
//                outputStream.write((byte) r);
//                numberOfBytesCopied++;
//            }
//
//            inputStream.close();
//            outputStream.close();
//
//            return true;
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return false;
    }

    private File getFile(String id) {
        return this.fileService.getFile(id);
    }

    private InputStream downloadFile(DriveService service, com.google.api.services.drive.model.File file) throws IOException {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                return driveService.getDrive().files().get(file.getId()).executeMediaAsInputStream();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on DriveMimeTypes.
            return null;
        }
    }
}
