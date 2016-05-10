package main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.*;
import io.*;
import model.types.MimeType;
import org.api.change.ChangeService;
import org.api.UpdateService;
import org.configuration.Configuration;
import org.api.FileService;
import database.DatabaseModule;
import database.repository.DatabaseService;
import model.tree.TreeBuilder;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;

/**
 * JDriveMain class
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class JDriveMain {

    private static Injector injector;
    private static TreeBuilder treeBuilder;
    private static FileService fileService;
    private static ChangeService changeService;
    private static UpdateService updateService;
    private static DatabaseService dbService;
    private static ChangeRepository changeRepository;
    private static FileRepository fileRepository;
    private static ChangeTree changeTree;
    private static ChangeInterpreted changeInterpreted;
    private static Configuration configReader;

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

        if( ! isSetUp()) {
            boolean setUpSuccess = false;
            try {
                setUpSuccess = setUpJDrive();
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }

            if( ! setUpSuccess) {
                logger.error("Cannot set up the application");
                System.exit(1);
            }

            //Create root folder
            try{
                initRootFolder();
            } catch (Exception exception){

            }

            try{
                initWrite();
            }catch (Exception exception) {
                logger.error(exception.getMessage());
            }

            try {
                setUpChanges();
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }
        }

        try{
            getLastChanges();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        try {
            applyLastChanges();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static boolean isSetUp()
    {
        Path rootPath = FileSystems.getDefault().getPath(configReader.getRootFolder());

        return Files.exists(rootPath);
    }

    private static void applyLastChanges() {
        try{
            initWrite();
        }catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        GraphDatabaseService graphDB = dbService.getGraphDB();

        Queue<Node> changeQueue = changeRepository.getUnprocessed();

        logger.debug("Change queue: " + changeQueue.size());

        for (Node changeNode : changeQueue) {

            try {
                ChangeStruct changeStruct = changeInterpreted.execute(changeNode);
                NeedNameInterface service = ChangeFactory.getWriter(changeStruct);

                boolean result = false;

                result = service.execute();

                if(result){
                    result = changeRepository.markAsProcessed(changeNode);

                    if(changeStruct.getChange() != null && changeStruct.getChange().getFile() != null) {
                        result = fileRepository.update(changeStruct.getFileNode(), changeStruct.getChange().getFile());
                        if(service instanceof MoveService){
                            fileRepository.updateParentRelation(changeStruct.getFileNode(), changeStruct.getNewParentNode());
                        } else if (service instanceof DeleteService) {
                            fileRepository.delete(changeStruct.getChange().getFileId());
                        } else if (service instanceof TrashedService){
                            fileRepository.markasDeleted(changeStruct.getFileNode());
                        }
                    }
                }
////
//                logger.debug("Change: " + changeRepository.getId(changeNode) + " - Result: " + result);
//
//                if(result == false)
//                    logger.error(changeStruct.toString());

            }catch(Exception exception) {
                exception.printStackTrace();
            }

//            System.exit(0);
//

        }
    }

    private static void initRootFolder(){
        Node rootNode = fileRepository.getRootNode();

        WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(rootNode));
        writer.setNode(rootNode);

        String path = dbService.getNodeAbsolutePath(rootNode);

        boolean result = writer.write(path);

        if (result) {
            result = fileRepository.markAsProcessed(rootNode);
        }
    }

    private static void getLastChanges() throws Exception {
        Long lastChangeId = changeRepository.getLastChangeId();

        ChangeService changeService = injector.getInstance(ChangeService.class);
        List<Change> changeList = changeService.getAll(++lastChangeId);

        logger.debug("Lastest change id: " + lastChangeId);
        logger.debug("New changes total: " + changeList.size());

        boolean result = changeTree.execute(changeList);
    }

    private static void initWrite() throws Exception {
        Queue<Node> fileQueue = fileRepository.getUnprocessedQueue();

        logger.debug("Size: " + fileQueue.size());

        while (!fileQueue.isEmpty()) {
            Node node = fileQueue.remove();

            WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(node));
            writer.setNode(node);

            String path = dbService.getNodeAbsolutePath(node);

            boolean result = writer.write(path);

            if (result) {
                result = fileRepository.markAsProcessed(node);
            }

            if(result){
                logger.error("Success to write: " + path);
            } else {
                logger.error("Failed to write: " + path);
                writer.delete(path);
            }
        }
    }

    private static void initJDrive() {
        injector = Guice.createInjector(new DatabaseModule());
    }

    private static void initServices() {
        treeBuilder = injector.getInstance(TreeBuilder.class);
        fileService = injector.getInstance(FileService.class);
        changeService = injector.getInstance(ChangeService.class);
        updateService = injector.getInstance(UpdateService.class);
        dbService = injector.getInstance(DatabaseService.class);
        changeRepository = injector.getInstance(ChangeRepository.class);
        fileRepository = injector.getInstance(FileRepository.class);
        changeTree = injector.getInstance(ChangeTree.class);
        changeInterpreted = injector.getInstance(ChangeInterpreted.class);

        try {
            configReader = new Configuration();
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

    }

    private static boolean setUpJDrive() throws Exception {
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

    private static void setUpMonitor() {
//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static void setUpChanges() throws IOException, Exception {
        List<Change> changeList = changeService.getAll(null);

        changeList.forEach(changeRepository::addChange);

        //During initialiazation build list of changes which are already applied to the file
        changeList.forEach(changeRepository::update);

    }

    private static Configuration setConfiguration() throws IOException {
        Configuration configConfiguration = new Configuration();

        return configConfiguration;
    }

    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
//                graphDb.shutdown();
            }
        });
    }
}