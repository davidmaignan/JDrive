package org.main;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.api.file.FileService;
import org.model.TreeBuilder;
import org.model.TreeNode;
import org.signin.DriveService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * JDriveMain class
 * <p/>
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

        System.out.println(result.size());

        TreeBuilder treeBuilder = new TreeBuilder(result);

        TreeBuilder.printTree(treeBuilder.getRoot());

        System.out.println("End");
    }
}