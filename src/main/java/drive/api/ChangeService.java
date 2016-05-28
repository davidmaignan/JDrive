package drive.api;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.StartPageToken;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ChangeService - Retrieve the list of changes
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ChangeService {
    private static Logger logger = LoggerFactory.getLogger(ChangeService.class);
    private DriveService driveService;
    private String fields;
    public ChangeService(){}

    @Inject
    public ChangeService(DriveService driveService){
        this.driveService = driveService;
        fields = "changes,kind,newStartPageToken,nextPageToken";
    }

    /**
     * Retrieve a list of Change resources.
     *
     * @param startPageToken ID of the change to start retrieving subsequent changes from or {@code null}.
     *
     * @return List of Change resources.
     */
    public List<Change> getAll(String startPageToken) throws IOException {
        List<Change> result = new ArrayList<>();

        Drive.Changes.List request = driveService.getDrive().changes().list(startPageToken).setFields(fields);

        do {
            try {
                ChangeList changes = request.execute();
                result.addAll(changes.getChanges());
                request.setPageToken(changes.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    public StartPageToken getStartPageToken(){
        try {
            return driveService.getDrive().changes().getStartPageToken().execute();
        } catch (IOException exception){
            logger.error("Cannot retrieve getStartPageToken");
        }

        return null;
    }
}
