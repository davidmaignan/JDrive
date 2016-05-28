package main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.StartPageToken;
import com.google.inject.Guice;
import com.google.inject.Injector;
import configuration.*;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import drive.change.services.ConverterService;
import drive.change.model.ValidChange;
import drive.change.services.ChangeFactoryService;
import drive.change.services.ChangeInterface;
import io.*;
import drive.api.ChangeService;
import drive.api.FileService;
import database.DatabaseModule;
import database.repository.DatabaseService;
import io.filesystem.modules.FileSystemModule;
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
    private static FileService fileService;
    private static ChangeService changeService;
    private static DatabaseService dbService;
    private static FileRepository fileRepository;
    private static ConverterService converterService;
    private static Configuration configReader;
    private static Root root;

    private static Logger logger = LoggerFactory.getLogger(JDriveMain.class);

    /**
     * Main
     *
     * @param args String[]
     * @throws IOException
     * @throws Throwable
     */
    public static void main(String[] args) throws Throwable {
        initJDrive();
        initServices();
        setUp();

        try{
            getLastChanges();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static void setUp(){
        if( ! isSetUp()) {

            initRoot();

            boolean setUpSuccess = false;
            try {
                setUpSuccess = setUpJDrive();
            } catch (Exception exception) {
                logger.error(exception.getMessage());
            }

            if (!setUpSuccess) {
                logger.error("Cannot set up the application");
                System.exit(1);
            }

            try {
                initWrite();
            } catch (Exception exception) {
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

    private static void getLastChanges() throws Exception{

        String startPageToken = fileRepository.getStartTokenPage();

        logger.debug(startPageToken);

        List<Change> changeList = changeService.getAll(startPageToken);

        logger.debug("Change list: " + changeList.size());

        List<CustomChange> customChangeList = new ArrayList<>();

        for(Change change : changeList){
            ValidChange validChange = new ValidChange(fileRepository);
            validChange.execute(change);

            if(validChange.isValid()){
                customChangeList.add(converterService.execute(change));
            }
        }

        for(CustomChange customChange: customChangeList){
            logger.debug(customChange.toString());
        }

//        //Important: google drive does not return changes in right order.
//        //Sort manually based on ChangeType and position (depth) of the file in the tree structure.
        Collections.sort(customChangeList);

        while(! customChangeList.isEmpty()){
            CustomChange customChange = customChangeList.remove(0);
            try{
                ChangeInterface service = ChangeFactoryService.get(customChange);
                logger.debug(service.getClass().getSimpleName());
                logger.error(customChange.toString());

                boolean result = service.execute();

                // @todo Implement Promise pattern or a reference to fix async between db and I/O
                if(result == false){
                    logger.error(customChange.toString());
                    ValidChange validChange = new ValidChange(fileRepository);
                    validChange.execute(customChange.getChange());

                    if(validChange.isValid()){
                        customChangeList.add(converterService.execute(customChange.getChange()));
                    }
                }
            }catch(Exception exception) {
                customChangeList.add(customChange);
                exception.printStackTrace();
            }
        }

        setUpChanges();
    }

    private static void initWrite() throws Exception {
        Queue<Node> fileQueue = fileRepository.getUnprocessedQueue();

        while (!fileQueue.isEmpty()) {
            Node node = fileQueue.remove();

            WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(node));
            writer.setFileId(fileRepository.getFileId(node));

            String path = dbService.getNodeAbsolutePath(node);

            logger.debug(path);

            boolean result = writer.write(path);

            if (result) {
                result = fileRepository.markAsProcessed(node);
            }
        }
    }

    private static void initJDrive() {
        injector = Guice.createInjector(
                new DatabaseModule(),
                new FileSystemModule()
        );
    }

    private static boolean initRoot(){
        if(fileRepository.getRootNode() != null && root.exists()) {
            return true;
        }

        File rootFile = fileService.getRoot();

        if(root == null){
            logger.error("Cannot retrieve the root file from the api ");
            System.exit(1);
        }

        if(fileRepository.createRootNode(rootFile) && root.write("")) {
            return fileRepository.markAsProcessed(fileRepository.getRootNode());
        }

        logger.error("An error has occured when creating the root of the application");
        System.exit(1);

        return false;
    }

    private static void initServices() {
        fileService = injector.getInstance(FileService.class);
        changeService = injector.getInstance(ChangeService.class);
        dbService = injector.getInstance(DatabaseService.class);
        fileRepository = injector.getInstance(FileRepository.class);
        converterService = injector.getInstance(ConverterService.class);
        root = injector.getInstance(Root.class);

        try {
            configReader = new Configuration();
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }
    }

    private static boolean setUpJDrive() throws Exception {
        List<File> result = fileService.getAll();

        Node rootNode = fileRepository.getRootNode();

        String nodeId = fileRepository.getFileId(rootNode);

        TreeBuilder tree = new TreeBuilder(nodeId);
        tree.build(result);

        fileRepository.save(tree.getRoot());

        return true;
    }

    private static void setUpMonitor() {
//        MonitorService monitorService = injector.getInstance(MonitorService.class);
//        monitorService.start();
    }

    private static void setUpChanges() throws IOException, Exception {
        StartPageToken token = changeService.getStartPageToken();

        if(token != null){
            String startToken = token.getStartPageToken();
            fileRepository.updateStartPageToken(startToken);
            logger.debug("Start token: " + startToken);
        }
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