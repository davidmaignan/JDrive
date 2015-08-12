package org.main;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tinkerpop.blueprints.Vertex;
import org.api.ChangeService;
import org.api.UpdateService;
import org.configuration.Configuration;
import org.api.FileService;
import org.db.DatabaseService;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
        Logger logger = LoggerFactory.getLogger(JDriveMain.class);
        logger.error("test logger");

        ArrayList<AbstractModule> moduleList = new ArrayList<>();
        moduleList.add(new TreeModule());

        Injector injector           = Guice.createInjector(moduleList);
        TreeBuilder treeBuilder     = injector.getInstance(TreeBuilder.class);
        FileService fileService     = injector.getInstance(FileService.class);
        UpdateService updateService = injector.getInstance(UpdateService.class);
        DatabaseService dbService   = injector.getInstance(DatabaseService.class);

//        dbService.debug();

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
//        TreeBuilder.printTree(treeBuilder.getRoot());
        Boolean writeSuccess = injector.getInstance(TreeWriter.class).writeTree(treeBuilder.getRoot());

        if (writeSuccess) {
            dbService.save(treeBuilder.getRoot());
        }

        Configuration configReader = new Configuration();
        Long lastChangeId = Long.getLong(configReader.getProperty("lastChangeId"));

        System.out.println("Last change id: " + lastChangeId);

        ChangeService changeService = injector.getInstance(ChangeService.class);
        List<Change> changeList = changeService.getAll(null);

        Set<String> fileIdList = new HashSet<>();

        for (Change change : changeList){
            fileIdList.add(change.getFileId());
            lastChangeId = (lastChangeId == null)? change.getId() : Math.max(lastChangeId, change.getId());

            updateService.update(change);
        }

//
        //Monitor service
//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static Configuration setConfiguration() throws IOException{
        Configuration configConfiguration = new Configuration();

        return configConfiguration;
    }
}