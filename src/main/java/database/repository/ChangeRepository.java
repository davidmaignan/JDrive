package database.repository;

import com.google.api.services.drive.model.Change;
import com.google.inject.Inject;
import database.DatabaseConfiguration;
import database.Fields;
import database.RelTypes;
import configuration.Configuration;
import database.labels.ChangeLabel;
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

    public ChangeRepository(){}

    public ChangeRepository(GraphDatabaseService graphDb, Configuration configuration) {
        super(graphDb, configuration);
    }

    @Inject
    public ChangeRepository(DatabaseConfiguration dbConfig, Configuration configuration) {
        super(dbConfig, configuration);
    }

    /**
     * Delete a node by id
     *
     * @param node Node
     * @return boolean
     */
    public boolean delete(Node node) {
        String query = "match (file {identifier: '%s'}) match (file)<-[r*]-(m) " +
                "foreach (rel in r | delete rel) delete m with file match (file)-[r]->(m) delete r, file";
        try (Transaction tx = graphDB.beginTx()) {

            query = String.format(query, node.getProperty(Fields.ID));

            graphDB.execute(query);

            tx.success();

            return true;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    //@todo refactor - do not return null
    public String getId(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            String result = node.getProperty(Fields.ID).toString();

            tx.success();

            return result;
        } catch (Exception exception){
            return null;
        }
    }

    //@todo - what to return if failure ?
    public Boolean getDeleted(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            Boolean result = Boolean.valueOf(node.getProperty(Fields.DELETED).toString());

            tx.success();

            return result;
        } catch (Exception exception){
            return false;
        }
    }

    //@todo - what to return if failure ?
    public Boolean getProcessed(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            Boolean result = Boolean.valueOf(node.getProperty(Fields.PROCESSED).toString());

            tx.success();

            return result;
        } catch (Exception exception){
            return false;
        }
    }

    public String getFileId(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            String result = node.getProperty(Fields.FILE_ID).toString();

            tx.success();

            return result;
        } catch (Exception exception){
            return null;
        }
    }

    public Long getVersion(Node node){
        try(Transaction tx = graphDB.beginTx()) {
            Long result = new Long((long)node.getProperty(Fields.VERSION));

            tx.success();

            return result;
        } catch (Exception exception){
            return null;
        }
    }


    /**
     * Get all the unprocessed changes
     *
     * @return queue of changes order by id asc.
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
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return queueResult;
    }

    /**
     * Set processed as true
     * @param node
     * @return processed value updated
     */
    public boolean markAsProcessed(Node node) {
        try (Transaction tx = graphDB.beginTx()) {
            node.setProperty(Fields.PROCESSED, true);
            boolean result = (boolean)node.getProperty(Fields.PROCESSED);
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
    public boolean markAsDeleted(Node node) {
        try (Transaction tx = graphDB.beginTx()) {
            node.setProperty(Fields.DELETED, true);
            boolean result = (boolean)node.getProperty(Fields.DELETED);

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
    public boolean markAsTrashed(Node node) {
        try (Transaction tx = graphDB.beginTx()) {
            node.setProperty(Fields.TRASHED, true);
            boolean result = (boolean)node.getProperty(Fields.TRASHED);

            tx.success();

            return result;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Update change node once applied
     *
     * @param change Change
     * @return true on success
     */
    public boolean update(Change change) {

        String query = "match (change:Change {%s: %s}) " +
                "set change.%s=%b, change.%s=%b, change.%s=%b " +
                "return change";

        try(Transaction tx = graphDB.beginTx()){
            boolean trashed = getTrashed(change);
            query = String.format(query,
                    Fields.ID, change.getId(),
                    Fields.PROCESSED, true,
                    Fields.TRASHED, trashed,
                    Fields.DELETED, change.getDeleted());

            Result result = graphDB.execute(query);
            tx.success();

            return result.hasNext();
        }catch (Exception exception){
            logger.error(exception.getMessage());
        }

        return false;
    }

    /**
     * Get trashed label value if available
     *
     * @param change Change
     *
     * @return boolean
     */
    public boolean getTrashed(Change change) {
        return  change.getDeleted()
                && (change.getFile() != null
                                && change.getFile().getExplicitlyTrashed() != null
                                && change.getFile().getExplicitlyTrashed())
                && (change.getFile() != null
                                    && change.getFile().getLabels() != null
                                    && change.getFile().getLabels().getTrashed());
    }

    public long getLastChangeId() {
        long result = 0;

        String query = "match (change:Change) with change.%s as id ORDER BY id DESC LIMIT 1 RETURN id";

        query = String.format(query, Fields.ID);

        try(Transaction tx = graphDB.beginTx();
            Result queryResult = graphDB.execute(query))
        {
            if(queryResult.hasNext()) {
                result =  (long)queryResult.next().get("id");
            }

            tx.success();

            return result;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return result;
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
     * Add change node to the linked list
     *
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
            logger.error("Change: " + change.getId()
                    + " - " + change.getFileId()
                    + " - " +exception.getMessage()
            );
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
        changeNode.addLabel(new ChangeLabel());

        changeNode.setProperty(Fields.ID, change.getId());
        changeNode.setProperty(Fields.FILE_ID, change.getFileId());
        changeNode.setProperty(Fields.MODIFIED_DATE, change.getModificationDate().getValue());
        changeNode.setProperty(Fields.SELF_LINK, change.getSelfLink());
        changeNode.setProperty(Fields.DELETED, change.getDeleted());
        changeNode.setProperty(Fields.PROCESSED, false);
        changeNode.setProperty(Fields.TRASHED, false);
        changeNode.setProperty(Fields.VERSION, change.getFile().getVersion());

        return changeNode;
    }

    /**
     * Get the insertion point for a new change
     * @param nodeId
     * @return null | change node
     */
    private Node getInsertPoint(String nodeId) {
        String query = "match (file {identifier: '%s'}) optional match (file)<-[r:CHANGE*]-(m) " +
                "with m order by m.identifier DESC limit 1 return m";

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, nodeId));

            tx.success();

            return (result.hasNext()) ? (Node) result.next().get("m") : null;

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return null;
    }
}
