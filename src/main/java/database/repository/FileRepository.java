package database.repository;

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
 * Files & Folders are saved as a 'file' node in a tree structure.
 * The link between them is defined as a parent RelTypes.PARENT
 * The owner (start node) of the relation is the child, the end node (obviously a folder)
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

    /**
     * Set processed as true
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

        try (
                Transaction tx = graphDB.beginTx();
                Result queryResult = graphDB.execute(String.format(query, Fields.ID, node.getProperty(Fields.ID),
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
     * Get a queue of the unprocessed files/folders.
     *
     * @return queue of files/folders to be written ordered from the root.
     */
    public Queue<Node> getUnprocessedQueue() {

        logger.debug("test");

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
//            logger.error(exception.getMessage());
            exception.printStackTrace();
        }

        return queueResult;
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

        if (tNode.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, tNode.getCreatedDate().getValue());
        }

        if (tNode.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, tNode.getModifiedDate().getValue());
        }
    }
}
