package drive.api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * File Service - Retrieve files from the api
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileService {
    private static Logger logger = LoggerFactory.getLogger(FileService.class.getSimpleName());
    private DriveService driveService;

    public FileService(){}

    @Inject
    public FileService(DriveService driveService) throws IOException {
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

        String fields = "files(createdTime,explicitlyTrashed,id,mimeType,modifiedTime,name,parents,size,trashed,version)";

        Drive.Files.List request = driveService.getDrive().files().list().setFields(fields);

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getFiles());
                request.setPageToken(files.getNextPageToken());

            } catch (IOException e) {
                Logger logger = LoggerFactory.getLogger(this.getClass().getName());
                logger.error("Error: e %s", e.toString());
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null && request.getPageToken().length() > 0);

        return result;
    }

//    files(createdTime,explicitlyTrashed,id,mimeType,modifiedTime,name,parents,size,trashed,version)

    /**
     * Get producer by id
     * @param fileId
     * @return File|null
     */
    public File getFile(String fileId) {
        try {
            return driveService.getDrive().files().get(fileId).execute();
        } catch (IOException e) {

            logger.error("Error: Cannot retrieve the producer: " + fileId);
        }
        return null;
    }

    /**
     * Get InputStream for a fileId
     * @param id
     * @return
     * @throws IOException
     */
    public InputStream downloadFile(String id) throws IOException {
        try {
            return driveService.getDrive().files().get(id).executeMediaAsInputStream();
        } catch (IOException e) {
            logger.error("Cannot get producer from google drive api: " + id);
            return null;
        }
    }

    /**
     * Get root id
     *
     * @return File root of the drive.
     */
    public File getRoot(){
        try{
            return driveService.getDrive().files().get("root").execute();
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

        return null;
    }
}
