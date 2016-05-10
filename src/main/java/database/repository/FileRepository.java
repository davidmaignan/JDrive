package database.repository;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import database.Connection;
import database.DatabaseConfiguration;
import database.Fields;
import database.RelTypes;
import org.configuration.Configuration;
import model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
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

    public String getTitle(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            return node.getProperty(Fields.TITLE).toString();

        } catch (Exception exception){
            return null;
        }
    }

    public Long getVersion(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            return new Long((long)node.getProperty(Fields.VERSION));

        } catch (Exception exception){
            return null;
        }
    }

    public Node getRootNode(){
        Node result = null;
        try(Transaction tx = graphDB.beginTx()) {

            String query = "match (file:File {IsRoot: true}) return file";

            Result queryResult = graphDB.execute(query);

//            logger.debug(queryResult.resultAsString());

            if(queryResult.hasNext()){
                result = (Node)queryResult.next().get("file");
            }

            tx.success();

            return result;

        } catch (Exception exception){
            return null;
        }
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
    public boolean markasDeleted(Node fileNode) {
        try(Transaction tx = graphDB.beginTx())
        {
            fileNode.setProperty(Fields.DELETED, true);

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
    public boolean markasDeleted(String id) {
        String query = "match (file {%s:'%s'})<-[:%s*]-(m) " +
                "set file.deleted=true, m.deleted=true return file, m";

        query = String.format(query, Fields.ID, id, RelTypes.PARENT);

        try(Transaction tx = graphDB.beginTx())
        {
            Result result = graphDB.execute(query);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Set processed field as true
     * @param id
     * @return processed value updated
     */
    public boolean markAsProcessed(String id) {
        String query = "match (file:File {%s: '%s'}) set file.%s = %s return file.%s";

        try (
                Transaction tx = graphDB.beginTx();
                Result queryResult = graphDB.execute(String.format(query, Fields.ID, id,
                        Fields.PROCESSED, true, Fields.PROCESSED));
        ) {

            boolean result = false;

            if(queryResult.hasNext()) {
                result = (boolean)queryResult.next().get(String.format("file.%s", Fields.PROCESSED));
            }

            tx.success();

            return result;

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
        String query = "match (file:File {%s: '%s'}) set file.%s = %s return file.%s";

        try (Transaction tx = graphDB.beginTx()){

            node.setProperty(Fields.PROCESSED, true);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    public boolean update(Node fileNode, File file){
        try(Transaction tx = graphDB.beginTx()) {
            fileNode.setProperty(Fields.VERSION, file.getVersion());
            fileNode.setProperty(Fields.TITLE, file.getTitle());
            fileNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
            fileNode.setProperty(Fields.PROCESSED, true);
            fileNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedDate().getValue());

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
            Result result = graphDB.execute(String.format(query, Fields.PROCESSED, false));

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
        try(Transaction tx = graphDB.beginTx()) {

            if(this.getNodeById(file.getId()) != null) {
                return false;
            }

            Node dbNode = graphDB.createNode(DynamicLabel.label("File"));

            dbNode.addLabel(DynamicLabel.label("File"));
            dbNode.setProperty(Fields.ID, file.getId());
            dbNode.setProperty(Fields.TITLE, file.getTitle());
            dbNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
            dbNode.setProperty(Fields.IS_ROOT, false);
            dbNode.setProperty(Fields.PROCESSED, false);
            dbNode.setProperty(Fields.VERSION, file.getVersion());

            if (file.getCreatedDate() != null) {
                dbNode.setProperty(Fields.CREATED_DATE, file.getCreatedDate().getValue());
            }

            if (file.getModifiedDate() != null) {
                dbNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedDate().getValue());
            }

            Node parent = this.getNodeById(file.getParents().get(0).getId());

            dbNode.createRelationshipTo(parent, RelTypes.PARENT);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    public Node createNode(File file) throws Exception{
        try(Transaction tx = graphDB.beginTx()) {

            Node dbNode = graphDB.createNode(DynamicLabel.label("File"));

            dbNode.addLabel(DynamicLabel.label("File"));
            dbNode.setProperty(Fields.ID, file.getId());
            dbNode.setProperty(Fields.TITLE, file.getTitle());
            dbNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
            dbNode.setProperty(Fields.IS_ROOT, false);
            dbNode.setProperty(Fields.PROCESSED, false);
            dbNode.setProperty(Fields.VERSION, file.getVersion());

            if (file.getCreatedDate() != null) {
                dbNode.setProperty(Fields.CREATED_DATE, file.getCreatedDate().getValue());
            }

            if (file.getModifiedDate() != null) {
                dbNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedDate().getValue());
            }

            tx.success();

            return dbNode;
        }
    }

    public boolean createParentRelation(Node child, Node parent){
        try(Transaction tx = graphDB.beginTx()) {
            child.createRelationshipTo(parent, RelTypes.PARENT);
            tx.success();
        } catch (Exception exception) {
            logger.error(exception.getMessage());
            return false;
        }

        return true;
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
        dbNode.addLabel(DynamicLabel.label("File"));
        dbNode.setProperty(Fields.ID, tNode.getId());
        dbNode.setProperty(Fields.TITLE, tNode.getTitle());
        dbNode.setProperty(Fields.MIME_TYPE, tNode.getMimeType());
        dbNode.setProperty(Fields.IS_ROOT, tNode.isSuperRoot());
        dbNode.setProperty(Fields.PROCESSED, false);
        dbNode.setProperty(Fields.VERSION, tNode.getVersion());

        if (tNode.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, tNode.getCreatedDate().getValue());
        }

        if (tNode.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, tNode.getModifiedDate().getValue());
        }
    }
}
