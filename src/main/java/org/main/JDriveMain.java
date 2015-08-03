package org.main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.config.Reader;
import org.api.ChangeService;
import org.api.FileService;
import org.drive.FileSearch;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeModule;
import org.model.types.MimeType;
import org.writer.FileWriter;
import org.writer.TreeWriter;

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
        FileService fileService = injector.getInstance(FileService.class);

//        Reader configReader = new Reader();
//        Long lastChangeId = Long.getLong(configReader.getProperty("lastChangeId"));
//        ChangeService changeService = injector.getInstance(ChangeService.class);
//        List<Change> changeList = changeService.getAll(null);
//
//        Set<String> fileIdList = new HashSet<>();
//
//        for (Change change : changeList){
//            fileIdList.add(change.getFileId());
//            lastChangeId = (lastChangeId == null)? change.getId() : Math.max(lastChangeId, change.getId());
//        }
//
//        //Update last change id
//        configReader.writeProperty("lastChangeId", String.valueOf(lastChangeId));
//
//        FileSearch fs = injector.getInstance(FileSearch.class);
//        FileWriter fileWriter = injector.getInstance(FileWriter.class);
//
//        //Update modified files
//        for(String fileId : fileIdList){
//            File file = fileService.getFile(fileId);
//            if (file == null) {
//                System.out.println(fileId + " is null");
//                break;
//            }
//
//            String fullPath = fs.getAbsolutePath(file.getTitle());
//
//            fileWriter.write(fullPath, file);
//            System.out.println("I just rewrote:  " + file.getTitle() + " @ " + fs.getAbsolutePath(file.getTitle()));
//
//        }


        List<File> result = fileService.getAll();
        treeBuilder.build(result);
        TreeBuilder.printTree(treeBuilder.getRoot());
        Boolean writeSuccess = injector.getInstance(TreeWriter.class).writeTree(treeBuilder.getRoot());
//
        //Monitor service
//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static Reader setConfiguration() throws IOException{
        Reader configReader = new Reader();

        return configReader;
    }
}