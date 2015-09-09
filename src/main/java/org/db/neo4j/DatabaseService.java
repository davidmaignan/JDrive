package org.db.neo4j;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.db.DatabaseConfiguration;
import org.db.DatabaseServiceInterface;
import org.db.Fields;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.MissingFormatArgumentException;

/**
 * Graph database service - Implementation for Neo4j
 *
 * David Maignan <davidmaignan@gmail.com>
 */
@Singleton
public class DatabaseService implements DatabaseServiceInterface {
    private DatabaseConfiguration dbConfig;
    private GraphDatabaseService graphDB;
    private Logger logger;

    public DatabaseService(){}

    public DatabaseService(GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
        logger       = LoggerFactory.getLogger(this.getClass().getSimpleName());
    }

    @Inject
    public DatabaseService(DatabaseConfiguration dbConfig){
        this.dbConfig = dbConfig;
        logger        = LoggerFactory.getLogger(this.getClass().getSimpleName());

        Connection object = Connection.getInstance();

//        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(dbConfig.getDBPath());

        graphDB = object.getGraphDB();

        try (Transaction tx = graphDB.beginTx()) {
            graphDB.schema()
                    .constraintFor( DynamicLabel.label( "File" ) )
                    .assertPropertyIsUnique( Fields.ID )
                    .create();
            tx.success();
        } catch (Exception exception) {
            //@todo implement sl4js
            logger.error(exception.toString());
        }
    }

    /**
     * Get graphDB
     *
     * @return GraphDatabaseService
     */
    public GraphDatabaseService getGraphDB(){
        return graphDB;
    }

    /**
     *
     * @param node TreeNode
     */
    public void save(TreeNode node){
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
            //@todo implement sl4j
            logger.error("Cannot save the tree of nodes");
        }
    }

    public boolean save(Change change) {
        try (Transaction tx = graphDB.beginTx()) {

            Node node = graphDB.createNode();
            this.setNode(node, change);

            Node parentNode = this.getNodeById(change.getFile().getParents().get(0).getId());

            node.setProperty(Fields.PATH,
                    String.format(
                            "%s/%s",
                            parentNode.getProperty(Fields.PATH),
                            change.getFile().getTitle()
                    )
            );

            parentNode.createRelationshipTo(node, RelTypes.CHILD);

            tx.success();

            return true;

        } catch (Exception exception) {
            //@todo implement sl4j
            logger.error("failed to save change in db" + change.getFile().getTitle());
        }

        return false;
    }

    public boolean delete(String id) {
        String query1 = "match (file {identifier:'%s'}), (others) where (others)<-[:CHILD*]-(file) with others match (others)-[r]-() delete others, r";
        String query2 = "match (file {identifier:'%s'}) MATCH (file)-[r]-() delete r, file";

        try (
                Transaction tx = graphDB.beginTx();

            )
        {
            Result result = graphDB.execute(String.format(query1, id));
            Result result2 = graphDB.execute(String.format(query2, id));
            tx.success();

            return true;
        } catch (Exception exception) {
            //@todo implement sl4j
            logger.error("Fail to delete: " + id);
        }

        return false;
    }

    public Node getParent(String id) {
        Node resultNode = null;

        String query = "match (file {identifier: '%s'}) " +
                "OPTIONAL MATCH (folder)<-[:PARENT]-(file) return folder;";
        try (
                Transaction tx = graphDB.beginTx();
                Result result = graphDB.execute(String.format(query, id))
        )
        {
            if(result.hasNext()) {
                resultNode = (Node)result.next().get("folder");
            }

            tx.success();

        } catch (Exception exception) {

        }

        return resultNode;
    }

    /**
     * Get node by property
     *
     * @param property String
     * @param value String
     *
     * @return Node | null
     */
    public Node getNode(String property, String value) {
        Node node = null;
        try (Transaction tx = graphDB.beginTx()) {
            node = graphDB.findNode(DynamicLabel.label("File"), Fields.ID, "root");

            tx.success();
        } catch (Exception exception) {
            //@todo implement sl4j
            logger.error("failed to get node: " +  property + ": " + value);
        }

        return node;
    }

    /**
     * Get node by property
     *
     * @param value String
     *
     * @return Node | null
     */
    public Node getNodeById(String value) {
        Node node = null;

        String query = "match (n {identifier:'%s'}) return n";

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, value));

            tx.success();

            return (Node)result.next().get("n");

        } catch (Exception exception) {
            //@todo implement sl4j
        }

        return node;
    }

    /**
     * Get property for a node by it's identifier
     * @param id String
     * @param property String
     * @return String
     */
    public String getNodePropertyById(String id, String property) {
        String resultProperty = null;

        String query = "match (file {identifier: '%s'}) return file.%s as %s";

        try(Transaction tx = graphDB.beginTx();
            Result result = graphDB.execute(String.format(query, id, property, property))) {

            if(result.hasNext()) {
                resultProperty = String.valueOf(result.next().get(property));
            }

            tx.success();
        } catch (Exception exception) {
            //@todo implement sl4j
            exception.printStackTrace();
            System.exit(0);
        }

        return resultProperty;
    }

    /**
     * Update property for a specific node
     * @param id
     * @param property
     * @param value
     * @return
     */
    public Node update(String id, String property, String value) {
        Node resultNode = null;

        String query = "match (file {%s: '%s'}) " +
                "set file.%s = %s RETURN file";
        try (
                Transaction tx = graphDB.beginTx();
                Result result = graphDB.execute(String.format(query, Fields.ID, id, property, value));
        )
        {
            if(result.hasNext()) {
                resultNode = (Node)result.next().get("folder");
            }

            tx.success();

        } catch (Exception exception) {
            //@todo implement sl4j
            logger.error("Fail to update: " + id + " - " + exception.toString());

        }

        return resultNode;
    }

    /**
     * Update node from a change api request
     * @param change Change
     * @return boolean
     */
    public boolean update(Change change) {
        String id   = change.getFileId();
        File file   = change.getFile();
        String path = this.getNewPath(file);

        String query = "match (file {identifier: '%s'}) " +
                "set file.title = '%s' " +
                "set file.path = '%s' " +
                "set file.modifiedDate = '%s' return file";

        try (Transaction tx = graphDB.beginTx()) {
            Result resultUpdate = graphDB.execute(
                    String.format(
                            query,
                            id,
                            file.getTitle(),
                            path,
                            file.getModifiedDate()
                    )
            );

            Map<String, Object> row = resultUpdate.next();
            Node node = (Node)row.get("file");

            //Check if parent was changed
            String newParentId = change.getFile().getParents().get(0).getId();

            Node parentNode = this.getParent(id);

            if (parentNode == null) {
                throw new Exception("Error updating db with changeId: " + change.getFileId() + ". No parent found! Every node other than root should have a parent.");
            }

            if (! parentNode.getProperty(Fields.ID).equals(newParentId)) {
                String queryDeleteRelations = String.format(
                        "match (file {identifier:'%s'}) match (file)-[r:%s]-(m) delete r with m match (m)-[r:%s]->(file) delete r",
                        id,
                        RelTypes.PARENT,
                        RelTypes.CHILD
                );

                graphDB.execute(queryDeleteRelations);

                Node newParentNode = this.getNodeById(newParentId);

                node.createRelationshipTo(newParentNode, RelTypes.PARENT);
                newParentNode.createRelationshipTo(node, RelTypes.CHILD);
            }

            tx.success();

        } catch (Exception exception) {
            exception.printStackTrace();
            logger.error("Fail to update db: " + exception);
            return false;
        }

        return true;
    }

    /**
     * Build a path for a file
     * @param file File
     * @return String
     *
     * @throws MissingFormatArgumentException
     */
    private String getNewPath(File file) throws MissingFormatArgumentException{
        return String.format(
                "%s/%s",
                this.getNodePropertyById(
                        file.getParents().get(0).getId(),
                        Fields.PATH),
                file.getTitle()
        );
    }

    /**
      Set node property
     * @param dbNode Node
     * @param change Change
     */
    private void setNode(Node dbNode, Change change) {
        File file = change.getFile();

        dbNode.addLabel(DynamicLabel.label("File"));
        dbNode.setProperty(Fields.ID, file.getId());
        dbNode.setProperty(Fields.TITLE, file.getTitle());
        dbNode.setProperty(Fields.MIME_TYPE, file.getMimeType());
        dbNode.setProperty(Fields.IS_ROOT, false);

        if (file.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, file.getCreatedDate().getValue());
        }

        if (file.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, file.getModifiedDate().getValue());
        }
    }

    /**
     * Set node property
     * @param dbNode Node
     * @param tNode TreeNode
     */
    private void setNode(Node dbNode, TreeNode tNode) {
        dbNode.addLabel(DynamicLabel.label("File"));
        dbNode.setProperty(Fields.ID, tNode.getId());
        dbNode.setProperty(Fields.TITLE, tNode.getTitle());
        dbNode.setProperty(Fields.PATH, tNode.getAbsolutePath());
        dbNode.setProperty(Fields.MIME_TYPE, tNode.getMimeType());
        dbNode.setProperty(Fields.IS_ROOT, tNode.isSuperRoot());

        if (tNode.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, tNode.getCreatedDate().getValue());
        }

        if (tNode.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, tNode.getModifiedDate().getValue());
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

        parent.createRelationshipTo(childV, RelTypes.CHILD);
        childV.createRelationshipTo(parent, RelTypes.PARENT);

        if (node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }
}
