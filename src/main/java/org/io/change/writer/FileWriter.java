package org.io.change.writer;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import org.api.DriveService;
import org.api.FileService;
import org.db.Fields;
import org.db.neo4j.DatabaseService;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Write a file from change
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileWriter implements WriterChangeInterface {
    private final DriveService driveService;
    private final FileService fileService;
    private final DatabaseService dbService;

    @Inject
    public FileWriter(DatabaseService dbService, DriveService driveService, FileService fileService) {
        this.dbService = dbService;
        this.driveService = driveService;
        this.fileService  = fileService;
    }

    @Override
    public boolean write(Change change) {
        System.out.println(dbService);
        System.out.println(dbService.getNodeById("0AHmMPOF_fWirUk9PVA"));
        try {
            InputStream inputStream   = this.downloadFile(driveService, this.getFile(change.getFileId()));
            OutputStream outputStream = new FileOutputStream(this.getAbsolutePath(change));

            if (inputStream == null) {
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

    private File getFile(String id) {
        return this.fileService.getFile(id);
    }

    private InputStream downloadFile(DriveService service, File file) throws IOException {
        if (file.getDownloadUrl() != null && file.getDownloadUrl().length() > 0) {
            try {
                return driveService.getDrive().files().get(file.getId()).executeMediaAsInputStream();
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

    /**
     * Get file path
     *
     * @param change Change
     *
     * @return String
     */
    protected String getAbsolutePath(Change change) {

        System.out.println();

        return String.format(
                "%s/%s",
                dbService.getNodePropertyById(change.getFile().getParents().get(0).getId(), Fields.PATH),
                change.getFile().getTitle()
        );
    }
}
