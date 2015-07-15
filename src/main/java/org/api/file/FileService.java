package org.api.file;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import java.io.IOException;


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
