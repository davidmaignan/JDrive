package database.repository;

import com.google.api.services.drive.model.Change;
import database.Fields;
import database.RelTypes;
import org.configuration.Configuration;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Changes are saved as a linkedList starting from a File Node
 * from the eldest one to the newest
 *
 * The link between them is defined as a parent RelTypes.CHANGE
 *
 * eg: create (a:Node {n:"a"}), (r:Node {n:"r"}),(s:Node {n:"s"}),
 *      r-[:CHANGE]->a, s-[:CHANGE]->r
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class ChangeRepository extends DatabaseService {
    private static Logger logger = LoggerFactory.getLogger(ChangeRepository.class);

    public ChangeRepository(GraphDatabaseService graphDb, Configuration configuration) {
        super(graphDb, configuration);
    }

    public Node getChangeById(long value) {
        Node node = null;

        String query = "match (change:Change) where change.%s=%s return change" ;

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.ID, value));

            tx.success();

            if(result.hasNext()) {
                return (Node) result.next().get("change");
            }

        } catch (Exception exception) {
            logger.error("Fail to get change: " + value);
            logger.error(exception.getMessage());
        }

        return node;
    }

    /**
     * Add change node to a queue/linkedList of changes starting from a file node
     * @param change
     * @return true on success
     */
    public boolean addChange(Change change) {
        try (Transaction tx = graphDB.beginTx()) {

            //@todo: investigate why unique id in schema definition is not working.
            if (this.getChangeById(change.getId()) != null) {
                return false;
            }

            Node relationNode = this.getInsertPoint(change.getFileId());

            if (relationNode == null) {
                relationNode = this.getNodeById(change.getFileId());
            }

            createChangeNode(change).createRelationshipTo(relationNode, RelTypes.CHANGE);

            tx.success();

            return true;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Create a db change node from a drive change
     * @param change
     * @return Node
     */
    private Node createChangeNode(Change change) {
        Node changeNode = graphDB.createNode();
        changeNode.addLabel(DynamicLabel.label("Change"));

        changeNode.setProperty(Fields.ID, change.getId());
        changeNode.setProperty(Fields.FILE_ID, change.getFileId());
        changeNode.setProperty(Fields.MODIFIED_DATE, change.getModificationDate().getValue());
        changeNode.setProperty(Fields.SELF_LINK, change.getSelfLink());
        changeNode.setProperty(Fields.DELETED, change.getDeleted());
        changeNode.setProperty(Fields.PROCESSED, false);

        return changeNode;
    }

    /**
     * Get the insertion point for a new change
     * @param nodeId
     * @return null | change node
     */
    private Node getInsertPoint(String nodeId) {
        String query = "match (file {%s:'%s'}) match (file)<-[r:%s*]-(m) " +
                "with m, count(r) AS length order by length desc limit 1 return  m";

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.ID, RelTypes.CHANGE, nodeId));

            tx.success();

            return (result.hasNext()) ? (Node) result.next().get("m") : null;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return null;
    }

    /**
     * Get unprocessed changes queue
     * @return queue of changes to be apply from the oldest to newest
     */
    public Queue<Node> getUnprocessed() {
        Queue<Node> queueResult = new ArrayDeque<>();

        String query = "match (n)<-[r:CHANGE]-(m {%s: %b}) with r, m order by m.identifier asc return m";

        try(Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, Fields.PROCESSED, false));

            while(result.hasNext()){
                Node node = (Node)result.next().get("m");
                queueResult.add(node);
            }

            tx.success();
        }

        return queueResult;
    }
}
