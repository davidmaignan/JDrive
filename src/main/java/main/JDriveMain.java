package main;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.Fields;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.ChangeTree;
import io.DeleteService;
import io.MoveService;
import io.WriterFactory;
import io.WriterInterface;
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

import java.io.FileOutputStream;
import java.io.IOException;
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

//        try {
//            initChanges();
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }

//        try{
//            getLastChanges();
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }

        try {
            applyLastChanges();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        registerShutdownHook(dbService.getGraphDB());
    }

    private static void applyLastChanges() {
        GraphDatabaseService graphDB = dbService.getGraphDB();

        Queue<Node> changeQueue = changeRepository.getUnprocessed();

        for (Node changeNode : changeQueue) {

            Node fileNode = fileRepository.getFileNodeFromChange(changeNode);
//            String path =  dbService.getNodeAbsolutePath(fileNode);

            boolean result = false;

            try(Transaction tx = graphDB.beginTx()){

//                String fileVersion = fileNode.getProperty(Fields.VERSION);
//                Boolean isProcessed = (boolean) fileNode.getProperty(Fields.PROCESSED);

                //Check if same parent
                Node previousParentNode = fileRepository.getParent(changeNode.getProperty(Fields.FILE_ID).toString());

                Change change = changeService.get(changeNode.getProperty(Fields.ID).toString());

                String newParent = change.getFile().getParents().get(0).getId();
                String previousParent = previousParentNode.getProperty(Fields.ID).toString();

                logger.debug("newparent: " + newParent + " - previous:  " + previousParent);

//                if ( ! isProcessed) {
//                    logger.debug("NEW FILE TO CREATE");
//                    WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(fileNode));
//                    writer.setNode(fileNode);
//
//
//                    result = writer.write(path);
//
//                    if (result) {
//                        logger.info("Success to create new File: " + path);
//                    } else {
//                        logger.error("Failed to create new file: " + path);
//                    }
//                } else

                if ( ! newParent.equals(previousParent)) {
                    logger.debug("FILE/FOLDER TO MOVE");

//                    Node newParentNode = fileRepository.getNodeById(newParent);
//
//                    String newPath =
//                            String.format("%s/%s",
//                                    fileRepository.getNodeAbsolutePath(newParentNode),
//                                    change.getFile().getTitle()
//                                    );
//
//                    MoveService service = new MoveService(path, newPath);
//
//                    result = service.execute();
//
//                    if (result && fileRepository.updateParentRelation(fileNode, newParentNode)) {
//                        logger.info("Success to move file: " + path);
//
//                    } else {
//                        logger.error("Failed to move file: " + path);
//                    }

                } else if( change.getDeleted() || change.getFile().getLabels().getTrashed()) {
                    logger.debug("DELETE FILE");
//                    DeleteService service = new DeleteService(path);
//
//                    result = service.execute();

                } else if( ! MimeType.all().contains(change.getFile().getMimeType())) {
                    logger.debug("UPDATE FILE");

//                    WriterInterface writer = WriterFactory.getWriter(change.getFile().getMimeType());
//
//                    writer.setNode(fileNode);
//                    result = writer.write(path);
//                    if (result) {
//                        logger.info("Success to update file: " + path);
//                    } else {
//                        logger.error("Failed to update file: " + path);
//                    }
                } else{
                    result = true;
                }

//                if(result) {
//                    changeNode.setProperty(Fields.MODIFIED_DATE, change.getFile().getModifiedDate().getValue());
//                    fileNode.setProperty(Fields.VERSION, change.getFile().getVersion());
//                    fileNode.setProperty(Fields.PROCESSED, true);
//                    fileNode.setProperty(Fields.TITLE, change.getFile().getTitle());
//                    changeNode.setProperty(Fields.PROCESSED, true);
//                }

                tx.success();

            } catch (Exception exception) {

                try(Transaction tx = graphDB.beginTx()){
//                    changeNode.setProperty(Fields.MODIFIED_DATE, );
                    fileNode.setProperty(Fields.VERSION, changeNode.getProperty(Fields.VERSION));
                    fileNode.setProperty(Fields.PROCESSED, true);
                    changeNode.setProperty(Fields.PROCESSED, true);

                    tx.success();


                }catch(Exception e){

                }

                logger.error("Icit: " + exception.getMessage());
            }


//            try (Transaction tx = graphDB.beginTx()) {
//                String changeId = node.getProperty(Fields.ID).toString();
//                String fileId = node.getProperty(Fields.FILE_ID).toString();
//
//                Node fileNode = fileRepository.getNodeById(fileId);
//                Boolean isNew = (boolean) fileNode.getProperty(Fields.PROCESSED);
//
//                String fileParent = fileRepository.getParent(fileId).getProperty(Fields.ID).toString();
//
//                Change change = changeService.get(changeId);
//
//                String changeParent = change.getFile().getParents().get(0).getId();
//
//                logger.debug("Change id: " + changeId);
//                logger.debug("Node parent: " + fileParent);
//                logger.debug("Change parent: " + changeParent);
//
//                String path = fileRepository.getNodeAbsolutePath(fileNode);
//
//                if (change.getDeleted() || change.getFile().getLabels().getTrashed()) {
//                    logger.error("Delete: " + fileId + ":" + change.getFile().getTitle());
////                    DeleteService service = new DeleteService(path);
////                    boolean result = service.execute();
////
////                    if(result) {
////                        fileRepository.markasDeleted(fileId);
////                    }
////
////                    logger.debug("File %s has been deleted: %b", fileId, result);
//
//                } else if (isNew) {
//                    logger.error("New file/folder: " + fileId + ":" + change.getFile().getTitle());
////                    WriterInterface writer = WriterFactory.getWriter(MimeType.FILE);
////                    writer.setNode(fileNode);
////
////                    if(writer.write(path) && ! fileRepository.markAsProcessed(node)) {
////                        logger.error("Failed to write: " + path);
////                        writer.delete(path);
////                    } else {
////                        logger.error("Success to write: " + path);
////                    }
//
//
//                } else if (!fileParent.equals(changeParent)) {
//                    logger.error("Move file/folder: " + fileId + ":" + change.getFile().getTitle());
////                    Node newParentNode = fileRepository.getNodeById(changeParent);
////
////                    String newpath = String.format("%s/%s",
////                            fileRepository.getNodeAbsolutePath(newParentNode),
////                            change.getFile().getTitle()
////                    );
////
////                    MoveService service = new MoveService(path, newpath);
////                    if(service.execute()) {
////                        boolean result = fileRepository.updateRelationship(change.getFileId(), changeParent);
////
////                        if( ! result) {
////                            logger.error("File id: %s - Relationship is not updated. db corrupted", fileId);
////                            System.exit(0);
////                        }
////                    }
//                } else {
//
//
//                }
//
//                // File node.processed = false -> new file
//
//                //File node.proccessed = true
//                // - Trashed
//                // - deleted
//                // - different parent
//                // - update content
//                // - none of these ignore then mark as processed
//
//                tx.success();
//            } catch (Exception exception) {
//                exception.printStackTrace();
//            }
        }
    }

    private static void getLastChanges() throws Exception {
        Long lastChangeId = changeRepository.getLastChangeId();

        logger.debug("Lastest change id: " + lastChangeId);

        ChangeService changeService = injector.getInstance(ChangeService.class);
        List<Change> changeList = changeService.getAll(++lastChangeId);

        boolean result = changeTree.execute(changeList);
    }

    private static void initChanges() throws Exception {
        Queue<Node> changeQueue = changeRepository.getUnprocessed();

        while (!changeQueue.isEmpty()) {
            Node node = changeQueue.remove();

            GraphDatabaseService graphDB = dbService.getGraphDB();

            try (Transaction tx = graphDB.beginTx()) {
                Node fileNode = fileRepository.getNodeById(node.getProperty(Fields.FILE_ID).toString());

                String changeVersion = node.getProperty(Fields.VERSION).toString();
                Long changeId = ((long) node.getProperty(Fields.ID));
                String fileVersion = fileNode.getProperty(Fields.VERSION).toString();

                if (changeVersion.compareTo(fileVersion) <= 0) {
                    boolean result = changeRepository.markAsProcessed(node);

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

    private static void initWrite() throws Exception {
        Queue<Node> fileQueue = fileRepository.getUnprocessedQueue();

        logger.debug("Size: " + fileQueue.size());

        while (!fileQueue.isEmpty()) {
            Node node = fileQueue.remove();

            WriterInterface writer = WriterFactory.getWriter(dbService.getMimeType(node));
            writer.setNode(node);

            String path = dbService.getNodeAbsolutePath(node);

            if (writer.write(path) && !fileRepository.markAsProcessed(node)) {
                logger.error("Failed to write: " + path);
                writer.delete(path);
            } else {
                logger.error("Success to write: " + path);
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
//        Long lastChangeId = changeRepository.getLastChangeId();
        List<Change> changeList = changeService.getAll(null);

        for (Change change : changeList) {
            if (changeRepository.addChange(change)) {
                logger.debug("Change: " + change.getId() + " added");
            }
        }
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