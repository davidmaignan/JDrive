package database.repository;

import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import database.DatabaseConfiguration;
import database.Fields;
import database.labels.FileLabel;
import database.RelTypes;
import configuration.Configuration;
import model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * File repository.
 *
 * Files & Folders are saved as a 'file' node in the graph database
 * The link between them is defined as a parent RelTypes.PARENT
 * The owner (start node) of the relation is the child, the end node (the folder)
 * is the parent.
 *
 * Eg: create (root:Node {n:"root"}), (a:Node {n:"a"}), (b:Node {n:"b"}), (c:Node {n:"c"}),
 *      root<-[:PARENT]-a, root<-[:PARENT]-b, a<-[:PARENT]-c;
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileRepository extends DatabaseService {
    private Logger logger = LoggerFactory.getLogger(FileRepository.class);

    public FileRepository(){}

    public FileRepository(GraphDatabaseService graphDb, Configuration configuration) {
        super(graphDb, configuration);
    }

    @Inject
    public FileRepository(DatabaseConfiguration dbConfig, Configuration configuration) {
        super(dbConfig, configuration);
    }

    public String getName(Node node){
        String name = null;
        try(Transaction tx = graphDB.beginTx()) {
            name = node.getProperty(Fields.NAME).toString();

            tx.success();

            return name;
        } catch (Exception exception){
            return null;
        }
    }

    public boolean createRootNode(File file){
        try(Transaction tx = graphDB.beginTx()) {
            Node node = graphDB.createNode(new FileLabel());

            node.setProperty(Fields.IS_ROOT, true);
            node.setProperty(Fields.ID, file.getId());

            tx.success();
            return true;

        } catch (Exception exception){
            logger.error(exception.getMessage());
        }

        return false;
    }

    public Node getRootNode(){
        Node result = null;
        try(Transaction tx = graphDB.beginTx()) {
            Result queryResult = graphDB.execute(
                    String.format("match (file:File {%s: %b}) return file", Fields.IS_ROOT, true)
            );

            if(queryResult.hasNext()){
                result = (Node)queryResult.next().get("file");
            }

            tx.success();
        } catch (Exception exception){
            logger.error(exception.getMessage());
        }

        return result;
    }

    /**
     * Modifiy Parent relation when file/folder is moved to another directory
     *
     * @param child
     * @param parent
     * @return
     */
    public boolean updateParentRelation(Node child, Node parent) {
        try(Transaction tx = graphDB.beginTx()) {
            String queryCreateRelation = "match (child {%s:'%s'}), (parent {%s:'%s'})" +
                    " CREATE (child)-[r:%s]->(parent) return child, parent, r";

            queryCreateRelation = String.format(
                    queryCreateRelation,
                    Fields.ID,
                    child.getProperty(Fields.ID),
                    Fields.ID,
                    parent.getProperty(Fields.ID),
                    RelTypes.PARENT
            );

            String queryDeleteRelation = "match (child {%s:'%s'})-[r:%s]->() delete r";

            queryDeleteRelation = String.format(
                    queryDeleteRelation,
                    Fields.ID,
                    child.getProperty(Fields.ID),
                    RelTypes.PARENT
            );

            graphDB.execute(queryDeleteRelation);
            graphDB.execute(queryCreateRelation);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Update delete property recursively to all files and folders children
     * @param fileNode
     * @return true
     */
    public boolean markAsDeleted(Node fileNode) {
        if(fileNode == null)
            return false;

        try(Transaction tx = graphDB.beginTx()) {
            fileNode.setProperty(Fields.DELETED, true);
            fileNode.setProperty(Fields.PROCESSED, false);

            Iterable<Relationship> relationships = fileNode.getRelationships(RelTypes.PARENT, Direction.INCOMING);

            relationships.forEach( relationship -> {
                Node childNode = relationship.getStartNode();
                markAsDeleted(childNode);
            });

            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Update delete property recursively to all files and folders children
     * @param fileNode
     * @return true
     */
    public boolean markAsTrashed(Node fileNode) {
        if(fileNode == null)
            return false;

        try(Transaction tx = graphDB.beginTx()) {
            fileNode.setProperty(Fields.TRASHED, true);
            fileNode.setProperty(Fields.PROCESSED, false);

            Iterable<Relationship> relationships = fileNode.getRelationships(RelTypes.PARENT, Direction.INCOMING);

            relationships.forEach( relationship -> {
                Node childNode = relationship.getStartNode();
                markAsTrashed(childNode);
            });

            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Update delete property recursively to all files and folders children
     * @param fileNode
     * @return true
     */
    public boolean markAsUnTrashed(Node fileNode) {
        try(Transaction tx = graphDB.beginTx())
        {
            fileNode.setProperty(Fields.TRASHED, false);
            fileNode.setProperty(Fields.PROCESSED, false);

            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Update delete property recursively to all files and folders children
     * @param id
     * @return
     */
    public boolean markAsDeleted(String id) {
        String query = "match (file:File {%s:'%s'})<-[:%s*]-(m) " +
                "set file.deleted=true, m.deleted=true return file, m";

        try(Transaction tx = graphDB.beginTx()) {
            query = String.format(query, Fields.ID, id, RelTypes.PARENT);
            Result result = graphDB.execute(query);

            tx.success();

            return result.hasNext();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Set processed as true
     * @param node
     * @return processed value updated
     */
    public boolean markAsProcessed(Node node) {
        try (Transaction tx = graphDB.beginTx()){
            node.setProperty(Fields.PROCESSED, true);
            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    //@todo - Check why trashed is false
    public boolean update(Node fileNode, File file){
        try(Transaction tx = graphDB.beginTx()) {
            fileNode.setProperty(Fields.VERSION, file.getVersion());
            fileNode.setProperty(Fields.NAME, file.getName());
            fileNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
            fileNode.setProperty(Fields.PROCESSED, true);
            fileNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedTime().getValue());
            fileNode.setProperty(Fields.DELETED, false);
            fileNode.setProperty(Fields.TRASHED, false);

            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Get a queue of the unprocessed files/folders.
     *
     * @return queue of files/folders to be written ordered from the root.
     */
    public Queue<Node> getUnprocessedQueue() {
        Queue<Node> queueResult = new ArrayDeque<>();

        String query = "match (file:File {%s: %b}) optional match (file)<-[r:PARENT]-(m) " +
                "with file, r order by r.id asc return distinct file";

        try(Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(
                    String.format(query, Fields.PROCESSED, false)
            );

            while (result.hasNext()) {
                Node node = (Node)result.next().get("file");
                queueResult.add(node);
            }

            tx.success();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return queueResult;
    }

    /**
     * Get a queue of the trashed files/folders.
     *
     * @return queue of files/folders to be written ordered from the root.
     */
    public List<Node> getTrashedList() {
        List<Node> list = new ArrayList<>();

        String query = "match (file:File {%s: %b}) return distinct file";

        try(Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.TRASHED, true));

            while (result.hasNext()) {
                Node node = (Node)result.next().get("file");
                list.add(node);
            }

            tx.success();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return list;
    }

    /**
     * Get a queue of the trashed files/folders.
     *
     * @return queue of files/folders to be written ordered from the root.
     */
    public Queue<Node> getTrashedQueue() {
        Queue<Node> queueResult = new ArrayDeque<>();

        String query = "match (file:File {%s: %b, %s: %b}) optional match (file)<-[r:PARENT*]-(m:File {%s: %b})" +
                " with file order by file.%s desc return distinct file;";

        try(Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.TRASHED, true, Fields.PROCESSED, false,
                    Fields.IS_ROOT, true, Fields.CREATED_DATE));

            while (result.hasNext()) {
                Node node = (Node)result.next().get("file");
                queueResult.add(node);
            }

            tx.success();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return queueResult;
    }

    /**
     * Get a queue of the deleted files/folders.
     *
     * @return queue of files/folders to be written ordered from the root.
     */
    public Queue<Node> getDeletedQueue() {
        Queue<Node> queueResult = new ArrayDeque<>();

        String query = "match (file:File {%s: %b, %s: %b}) optional match (file)<-[r:PARENT*]-(m:File {%s: %b})" +
                " with file order by file.%s desc return distinct file;";

        try(Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.DELETED, true, Fields.PROCESSED, false,
                    Fields.IS_ROOT, true, Fields.CREATED_DATE));

            while (result.hasNext()) {
                Node node = (Node)result.next().get("file");
                queueResult.add(node);
            }

            tx.success();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return queueResult;
    }

    public boolean createIfNotExists(File file) {
        if(this.getNodeById(file.getId()) != null) {
            return false;
        }

        try(Transaction tx = graphDB.beginTx()) {
            Node dbNode = graphDB.createNode(new FileLabel());
            dbNode.setProperty(Fields.ID, file.getId());
            dbNode.setProperty(Fields.NAME, file.getName());
            dbNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
            dbNode.setProperty(Fields.IS_ROOT, false);
            dbNode.setProperty(Fields.PROCESSED, false);
            dbNode.setProperty(Fields.VERSION, file.getVersion());
            dbNode.setProperty(Fields.CREATED_DATE, file.getCreatedTime().getValue());

            if (file.getModifiedTime() != null) {
                dbNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedTime().getValue());
            } else {
                dbNode.setProperty(Fields.MODIFIED_DATE, file.getCreatedTime().getValue());
            }

            Node parent = this.getNodeById(file.getParents().get(0));

            Relationship relation = dbNode.createRelationshipTo(parent, RelTypes.PARENT);

            tx.success();

            return relation != null;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

//    public Node createNode(File file) throws Exception{
//        try(Transaction tx = graphDB.beginTx()) {
//
//            Node dbNode = graphDB.createNode(new FileLabel());
//
//            dbNode.addLabel(new FileLabel());
//            dbNode.setProperty(Fields.ID, file.getId());
//            dbNode.setProperty(Fields.NAME, file.getName());
//            dbNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
//            dbNode.setProperty(Fields.IS_ROOT, false);
//            dbNode.setProperty(Fields.PROCESSED, false);
//            dbNode.setProperty(Fields.VERSION, file.getVersion());
//            dbNode.setProperty(Fields.DELETED, false);
//            dbNode.setProperty(Fields.TRASHED, isTrash(file));
//
//            if (file.getCreatedTime() != null) {
//                dbNode.setProperty(Fields.CREATED_DATE, file.getCreatedTime().getValue());
//            }
//
//            if (file.getModifiedTime() != null) {
//                dbNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedTime().getValue());
//            }
//
//            tx.success();
//
//            return dbNode;
//        } catch (Exception exception) {
//            logger.error(exception.getMessage());
//        }
//
//        return null;
//    }

    public boolean createParentRelation(Node child, Node parent){
        try(Transaction tx = graphDB.beginTx()) {
            Relationship relationship = child.createRelationshipTo(parent, RelTypes.PARENT);

            tx.success();

            return relationship != null;
        } catch (Exception exception) {
            String message = String.format("Failed to create relation parent between: %s and %s. %s",
                    child,
                    parent,
                    exception.getMessage()
            );
            logger.error(message);
        }

        return false;
    }

    /**
     * Save a treeNode
     *
     * @param node TreeNode
     */
    public void save(TreeNode node) {
        try (Transaction tx = graphDB.beginTx()) {

            Node rootNode = graphDB.createNode();
            this.setNode(rootNode, node);

            if (node.getChildren().size() > 0) {
                for (TreeNode child : node.getChildren()) {
                    save(rootNode, child);
                }
            }

            tx.success();

        } catch (Exception exception) {
            logger.error("Cannot save the tree of nodes");
            logger.error(exception.getMessage());
        }
    }

    /**
     * Recursively save a tree of nodes
     *
     * @param parent Node
     * @param node   TreeNode
     */
    private void save(Node parent, TreeNode node) {
        Node childV = graphDB.createNode();
        this.setNode(childV, node);

        childV.createRelationshipTo(parent, RelTypes.PARENT);

        if (node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }

    /**
     * Set node property
     *
     * @param dbNode Node
     * @param tNode  TreeNode
     */
    private void setNode(Node dbNode, TreeNode tNode) {
        dbNode.addLabel(new FileLabel());
        dbNode.setProperty(Fields.ID, tNode.getId());
        dbNode.setProperty(Fields.NAME, tNode.getName());
        dbNode.setProperty(Fields.MIME_TYPE, tNode.getMimeType());
        dbNode.setProperty(Fields.IS_ROOT, tNode.isRoot());
        dbNode.setProperty(Fields.PROCESSED, false);
        dbNode.setProperty(Fields.TRASHED, false);
        dbNode.setProperty(Fields.DELETED, false);
        dbNode.setProperty(Fields.VERSION, tNode.getVersion());

        if (tNode.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, tNode.getCreatedDate().getValue());
        }

        if (tNode.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, tNode.getModifiedDate().getValue());
        }
    }

    /**
     * Get trashed label value if available
     *
     * @return boolean
     */
    private boolean isTrash(File file){
         return (file != null
                && file.getExplicitlyTrashed() != null
                && file.getExplicitlyTrashed())
                 && file.getTrashed();
    }
}
