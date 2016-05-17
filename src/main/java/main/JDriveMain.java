package main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import drive.change.services.ConverterService;
import drive.change.model.ChangeTree;
import drive.change.model.ValidChange;
import drive.change.services.ChangeFactoryService;
import drive.change.services.ChangeInterface;
import io.*;
import drive.api.change.ChangeService;
import configuration.Configuration;
import drive.api.FileService;
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
    private static DatabaseService dbService;
    private static ChangeRepository changeRepository;
    private static FileRepository fileRepository;
    private static ChangeTree changeTree;
    private static ConverterService converterService;
    private static Configuration configReader;
    private static Delete delete;
    private static Trashed trashed;

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
        setUp();

        try{
            getLastChanges();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try {
            applyLastChanges();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        try{
            applyTrashed();
        } catch (Exception exception){
            exception.printStackTrace();
        }

        try{
            applyDeleted();
        } catch (Exception exception){
            exception.printStackTrace();
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static void setUp(){
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
    }

    private static boolean isSetUp(){
        Path rootPath = FileSystems.getDefault().getPath(configReader.getRootFolder());

        return Files.exists(rootPath);
    }

    private static void applyLastChanges() {
        try{
            initWrite();
        }catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        Queue<Node> changeQueue = changeRepository.getUnprocessed();
        logger.debug("Change queue: " + changeQueue.size());

        for (Node changeNode : changeQueue) {

            try {
                CustomChange customChange = converterService.execute(changeNode);
                ChangeInterface service = ChangeFactoryService.get(customChange);

                boolean result = service.execute();

                if(result == false)
                    logger.error(customChange.toString());

            }catch(Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void applyTrashed(){
        Queue<Node> queue = fileRepository.getTrashedQueue();

        logger.debug("File to trashed: " + queue.size());

        while (! queue.isEmpty()){
            trashed.execute(queue.remove());
        }
    }

    private static void applyDeleted(){
        Queue<Node> queue = fileRepository.getDeletedQueue();

        logger.debug("File to deleted: " + queue.size());

        while (! queue.isEmpty()){
            delete.execute(queue.remove());
        }
    }

    private static void initRootFolder(){
        Node rootNode = fileRepository.getRootNode();

        WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(rootNode));

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

        List<ValidChange> validChangeList = new ArrayList<>();

        for(Change change : changeList){
            ValidChange validChange = new ValidChange(fileRepository);
            validChange.execute(change);

            if(validChange.isValid())
                validChangeList.add(validChange);
        }

        logger.debug("New valid changes total: " + validChangeList.size());

        changeTree.execute(validChangeList);
    }

    private static void initWrite() throws Exception {
        Queue<Node> fileQueue = fileRepository.getUnprocessedQueue();

        logger.debug("Size: " + fileQueue.size());

        while (!fileQueue.isEmpty()) {
            Node node = fileQueue.remove();

            WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(node));
            writer.setFileId(fileRepository.getFileId(node));

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
        dbService = injector.getInstance(DatabaseService.class);
        changeRepository = injector.getInstance(ChangeRepository.class);
        fileRepository = injector.getInstance(FileRepository.class);
        changeTree = injector.getInstance(ChangeTree.class);
        converterService = injector.getInstance(ConverterService.class);
        delete = injector.getInstance(Delete.class);
        trashed = injector.getInstance(Trashed.class);

        try {
            configReader = new Configuration();
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

    }

    private static boolean setUpJDrive() throws Exception {
        List<File> result = fileService.getAll();

        treeBuilder.build(result);
        dbService.save(treeBuilder.getRoot());

        return true;
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