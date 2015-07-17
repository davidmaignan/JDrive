package org.main;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;

import com.google.api.services.drive.model.ParentReference;
import org.api.file.FileService;
import org.config.Reader;
import org.model.TreeBuilder;
import org.model.TreeNode;
import org.model.TreeWriter;
import org.signin.DriveService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


/**
 * JDriveMain class
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class JDriveMain {

    /**
     * Main
     *
     * @param args
     * @throws IOException
     * @throws Throwable
     */
    public static void main(String[] args) throws IOException, Throwable {

        // Build a new authorized API client service.
        DriveService driveService = new DriveService();
        Drive service             = driveService.getDrive();
        FileService fileService   = new FileService(service);

        List<File> result = new ArrayList<>();

        Drive.Files.List request = service.files().list().setMaxResults(1000);

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

        //Init config
        Reader configReader = new Reader();

        TreeBuilder treeBuilder = new TreeBuilder(configReader.getProperty("rootFolder"), result);

        TreeBuilder.printTree(treeBuilder.getRoot());

        TreeNode root = treeBuilder.getRoot();

        TreeWriter treeWriter = new TreeWriter(root, service);
        treeWriter.write();
    }

    private static Reader setConfiguration() throws IOException{
        Reader configReader = new Reader();

        return configReader;
    }
}