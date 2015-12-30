package org.main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.api.change.ChangeService;
import org.api.UpdateService;
import org.configuration.Configuration;
import org.api.FileService;
import database.DatabaseModule;
import database.repository.DatabaseService;
import org.io.ChangeExecutor;
import org.io.ChangeInterface;
import org.model.tree.TreeBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * JDriveMain class
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class JDriveMain {

    private static Injector injector;
    private static TreeBuilder treeBuilder;
    private static FileService fileService;
    private static UpdateService updateService;
    private static DatabaseService dbService;
    private static ChangeExecutor changeExecutor;

    private static Logger logger = LoggerFactory.getLogger(JDriveMain.class);

    /**
     * Main
     *
     * @param args String[]
     * @throws IOException
     * @throws Throwable
     */
    public static void main(String[] args) throws IOException, Throwable {
        initJDrive();
        initServices();

        boolean setUpSuccess = false;
        try {
            setUpSuccess = setUpJDrive();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        try {
            setUpChanges();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static void initJDrive() {
//        ArrayList<AbstractModule> moduleList = new ArrayList<>();
//        moduleList.add(new TreeModule());
//        moduleList.add(new DatabaseModule());

        injector = Guice.createInjector(new DatabaseModule());
    }

    private static void initServices() {
        treeBuilder     = injector.getInstance(TreeBuilder.class);
        fileService    = injector.getInstance(FileService.class);
        updateService  = injector.getInstance(UpdateService.class);
        dbService      = injector.getInstance(DatabaseService.class);
        changeExecutor = injector.getInstance(ChangeExecutor.class);

//        dbService.createTreeNodeType();
//        dbService.createParentType();
    }

    private static boolean setUpJDrive() throws Exception{
        List<File> result = fileService.getAll();

        treeBuilder.build(result);
        TreeBuilder.printTree(treeBuilder.getRoot());

        dbService.save(treeBuilder.getRoot());

        return true;

//        Boolean writeSuccess = injector.getInstance(TreeWriter.class).writeTree(treeBuilder.getRoot());
//
//        if (writeSuccess) {
//            dbService.save(treeBuilder.getRoot());
//        }
//
//        return writeSuccess;
    }

    private static void setUpMonitor(){
//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static void setUpChanges() throws IOException, Exception{
        Configuration configReader = new Configuration();

        Long lastChangeId = null;

        try {
            lastChangeId = Long.valueOf(configReader.getProperty("lastChangeId"));
        } catch (NumberFormatException exception) {

        }

        ChangeService changeService = injector.getInstance(ChangeService.class);
        List<Change> changeList = changeService.getAll(lastChangeId);

        System.out.println(changeList.size());

        int index = 0;

        for (Change change : changeList){
            System.out.println("index: " + index++);
            lastChangeId = (lastChangeId == null)? change.getId() : Math.max(lastChangeId, change.getId());



            dbService.addChange(change);

//            ChangeInterface service = updateService.update(change);
//            changeExecutor.addChange(service);
        }

//        System.out.println(changeExecutor.size());
//        changeExecutor.clean();
//        changeExecutor.debug();
//        changeExecutor.execute();

        for (ChangeInterface service : changeExecutor.getChangeListFailed()) {
            System.out.println("Failed to create: " + service.getChange().getFile().getTitle());
        }

        configReader.writeProperty("lastChangeId", lastChangeId);
//        System.out.println(lastChangeId);
    }

    private static Configuration setConfiguration() throws IOException{
        Configuration configConfiguration = new Configuration();

        return configConfiguration;
    }

    private static void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
//                graphDb.shutdown();
            }
        } );
    }
}