package org.main;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Children;
import com.google.api.services.drive.model.ChildList;
import com.google.api.services.drive.model.ChildReference;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.api.file.FileService;
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

        File file = service.files().get("root").execute();

        System.out.println("Title: " + file.getTitle());
        System.out.println("Description: " + file.getDescription());

//        Children.List request = service.children().list("root");
//        ChildList children = request.execute();
//        String id = children.getItems().get(1).getId();
//        File file1 = fileService.getFile(id);
//        System.out.println(file1);

        List<File> result = new ArrayList<>();

        Drive.Files.List request = service.files().list().setMaxResults(50);

        do {
            try {
                FileList files = request.execute();

                result.addAll(files.getItems());

                System.out.println(result.size());

                request.setPageToken(files.getNextPageToken());
            } catch (IOException e) {
                System.out.println("An error occurred: " + e);
                request.setPageToken(null);
            }
        } while (request.getPageToken() != null &&
                request.getPageToken().length() > 0 && result.size() < 100);

        for (File f : result) {
            System.out.println(f);
//            if(f.getMimeType().equals("application/vnd.google-apps.folder")) {
//                System.out.format("Folder: %s\n", f.getTitle());
//            } else {
//                System.out.format("File: %s\n", f.getTitle());
//            }
        }

//        do {
//            try {
//
//
//                for (ChildReference child : children.getItems()) {
//                    System.out.format("File Id: %s\n", child);
//                }
//                request.setPageToken(children.getNextPageToken());
//            } catch (IOException e) {
//                System.out.println("An error occurred: " + e);
//                request.setPageToken(null);
//            }
//        } while (request.getPageToken() != null &&
//                request.getPageToken().length() > 0);

//        FileList result = service.files().list()
//                .setMaxResults(10)
//                .execute();
//
//        List<File> files = result.getItems();
//        if (files == null || files.size() == 0) {
//            System.out.println("No files found.");
//        } else {
//            for (File file : files) {
//                System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
//            }
//        }
    }
}