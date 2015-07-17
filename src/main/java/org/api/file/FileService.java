package org.api.file;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.io.InputStream;


/**
 * FileService - Get file from Drive api
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-15.
 */
public class FileService {

    private boolean isFolder;

    private String id;

    /**
     * Drive service
     */
    private Drive drive;

    /**
     * Constructor
     * @param drive
     */
    public FileService(Drive drive){
        this.drive = drive;
    }

    /**
     * Get File by fileId
     * @param fileId
     * @return File
     *
     * @throws IOException
     */
    public File getFile(String fileId) throws IOException{
        return drive.files().get(fileId).execute();
    }

    /**
     * Download a file's content.
     * @param file Drive File instance.
     * @return InputStream containing the file's content if successful,
     *         {@code null} otherwise.
     */
    public InputStream downloadFile(File file) {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                // uses alt=media query parameter to request content
                return drive.files().get(file.getId()).executeMediaAsInputStream();
            } catch (IOException e) {
                // An error occurred.
                e.printStackTrace();
                return null;
            }
        } else {
            // The file doesn't have any content stored on Drive.
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setFolder(boolean isFolder) {
        this.isFolder = isFolder;
    }
}
