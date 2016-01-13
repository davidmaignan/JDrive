package main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import io.WriterFactory;
import io.WriterInterface;
import model.tree.TreeNode;
import org.api.change.ChangeService;
import org.api.UpdateService;
import org.configuration.Configuration;
import org.api.FileService;
import database.DatabaseModule;
import database.repository.DatabaseService;
import org.io.ChangeExecutor;
import org.io.ChangeInterface;
import model.tree.TreeBuilder;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
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
    private static ChangeRepository changeRepository;
    private static FileRepository fileRepository;
    private static ChangeExecutor changeExecutor;
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

//        boolean setUpSuccess = false;
//        try {
//            setUpSuccess = setUpJDrive();
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }
//
//        try{
//            initWrite();
//        }catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }
//
//        try {
//            setUpChanges();
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }
//
//        try {
//            initChanges();
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }

        try{
            run();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static void run() throws Exception{
        Long lastChangeId = null;

        try {
            lastChangeId = Long.valueOf(configReader.getProperty("lastChangeId"));
        } catch (NumberFormatException exception) {

        }

        ChangeService changeService = injector.getInstance(ChangeService.class);
        List<Change> changeList = changeService.getAll(lastChangeId);

        System.out.println("Change size: " + changeList.size());

        for (Change change : changeList){
            lastChangeId = (lastChangeId == null)? change.getId() : Math.max(lastChangeId, change.getId());
            changeRepository.addChange(change);
        }
    }

    private static void initChanges() throws Exception {
        Queue<Node> changeQueue = changeRepository.getUnprocessed();

        while( ! changeQueue.isEmpty()) {
            Node node = changeQueue.remove();

            GraphDatabaseService graphDB = dbService.getGraphDB();

            try (Transaction tx = graphDB.beginTx()) {
                Node fileNode = fileRepository.getNodeById(node.getProperty(Fields.FILE_ID).toString());

                String changeVersion = node.getProperty(Fields.VERSION).toString();
                Long changeId = ((long) node.getProperty(Fields.ID));
                String fileVersion = fileNode.getProperty(Fields.VERSION).toString();

                if(changeVersion.compareTo(fileVersion) <= 0) {
                    changeRepository.markAsProcessed(changeId);
                } else {
                    logger.debug("Need to apply the change");
                }

                tx.success();

            } catch (Exception exception) {
                logger.error("Cannot save the tree of nodes");
                logger.error(exception.getMessage());
            }
        }
    }

    private static void initWrite() throws Exception{
        Queue<Node> fileQueue = fileRepository.getUnprocessedQueue();

        logger.debug("Size: " + fileQueue.size());

        while( ! fileQueue.isEmpty()) {
            Node node = fileQueue.remove();

            WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(node));
            writer.setNode(node);

            String path = dbService.getNodeAbsolutePath(node);

            if(writer.write(path) && ! fileRepository.markAsProcessed(node)) {
                logger.error("Failed to write: " + path);
                writer.delete(path);
            } else {
                logger.error("Success to write: " + path);
            }
        }
    }

    private static void applyChanges(){
        Queue<Node> changeQueue = changeRepository.getUnprocessed();

        while ( ! changeQueue.isEmpty()) {
            Node node = changeQueue.remove();

            //file or folder

            //new parent / new file

            //Trashed or deleted
        }
    }

    private static void initJDrive() {
        injector = Guice.createInjector(new DatabaseModule());
    }

    private static void initServices() {
        treeBuilder      = injector.getInstance(TreeBuilder.class);
        fileService      = injector.getInstance(FileService.class);
        updateService    = injector.getInstance(UpdateService.class);
        dbService        = injector.getInstance(DatabaseService.class);
        changeRepository = injector.getInstance(ChangeRepository.class);
        fileRepository   = injector.getInstance(FileRepository.class);
        changeExecutor   = injector.getInstance(ChangeExecutor.class);

        try{
            configReader = new Configuration();
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

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


            changeRepository.addChange(change);

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