package org.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File Service - Retrieve files from the drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileService {
    private DriveService driveService;

    @Inject
    public FileService(DriveService driveService) {
        this.driveService = driveService;
    }

    /**
     * Get all the files on the drive
     *
     * @return List of files
     * @throws IOException
     */
    public List<File> getAll() throws IOException{
        List<File> result = new ArrayList<>();

        Drive.Files.List request = driveService.getDrive().files().list().setMaxResults(1000);

        do {
            try {

                FileList files = request.execute();

                result.addAll(files.getItems());

                request.setPageToken(files.getNextPageToken());

            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return result;
    }

    public File getFile(String id) {

        return null;
    }
}
