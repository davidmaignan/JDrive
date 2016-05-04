package org.api.change;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.inject.Inject;
import org.api.DriveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * ChangeService - Retrieve the list of changes
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ChangeService {

    private static Logger logger = LoggerFactory.getLogger(ChangeService.class);

    private DriveService driveService;

    public ChangeService(){}

    @Inject
    public ChangeService(DriveService driveService){
        this.driveService = driveService;
    }

    /**
     * Retrieve a list of Change resources.
     *
     * @param startChangeId ID of the change to start retrieving subsequent changes from or {@code null}.
     *
     * @return List of Change resources.
     */
    public List<Change> getAll(Long startChangeId) throws IOException {
        List<Change> result = new ArrayList<Change>();
        Drive.Changes.List request = driveService.getDrive().changes().list();

        if (startChangeId != null) {
            request.setStartChangeId(startChangeId);
        }
        do {
            try {
                ChangeList changes = request.execute();

                result.addAll(changes.getItems());
                request.setPageToken(changes.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0);

        return result;
    }

    public Change get(String changeId) {
        Change result = null;

        try {
            Change change = driveService.getDrive().changes().get(changeId).execute();

            result = change;
        } catch (IOException e) {
            logger.error(e.getMessage());
        }

        return result;
    }
}
