package org.main;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.api.file.FileService;
import org.config.Reader;
import org.model.TreeBuilder;
import org.model.TreeModule;
import org.model.TreeNode;
import org.model.TreeWriter;
import org.signin.DriveService;
import org.writer.FileModule;

import java.io.IOException;
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

        ArrayList<AbstractModule> moduleList = new ArrayList<>();
        moduleList.add(new TreeModule());

        Injector injector       = Guice.createInjector(moduleList);
        TreeBuilder treeBuilder = injector.getInstance(TreeBuilder.class);

        // Build a new authorized API client service.
        DriveService driveService = new DriveService();
        Drive service             = driveService.getDrive();

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


        treeBuilder.build(result);
        TreeBuilder.printTree(treeBuilder.getRoot());

        Boolean writeSuccess =  new TreeWriter().writeTree(treeBuilder.getRoot());
    }

    private static Reader setConfiguration() throws IOException{
        Reader configReader = new Reader();

        return configReader;
    }
}