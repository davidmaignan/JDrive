package drive.api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * File Service - Retrieve files from the api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileService {
    private DriveService driveService;

    public FileService(){}

    @Inject
    public FileService(DriveService driveService) {
        this.driveService = driveService;
    }

    /**
     * Get all the files on the api
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
                Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                logger.error("Error: e %s", e.toString());
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return result;
    }

    /**
     * Get file by id
     * @param fileId
     * @return File|null
     */
    public File getFile(String fileId) {
        try {
            return driveService.getDrive().files().get(fileId).execute();
        } catch (IOException e) {
            Logger logger = LoggerFactory.getLogger(this.getClass().getName());
            logger.error("Error: Cannot retrieve the file: " + fileId);
        }
        return null;
    }
}
