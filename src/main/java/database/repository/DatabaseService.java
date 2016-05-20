package database.repository;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import database.*;
import configuration.Configuration;
import database.labels.ChangeLabel;
import database.labels.FileLabel;
import model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Graph database apply - Implementation for Neo4j
 *
 * David Maignan <davidmaignan@gmail.com>
 */
@Singleton
public class DatabaseService implements DatabaseServiceInterface {

    private static boolean init = false;

    private DatabaseConfiguration dbConfig;
    private Configuration configuration;
    protected GraphDatabaseService graphDB;
    private static Logger logger = LoggerFactory.getLogger(DatabaseService.class.getSimpleName());

    public DatabaseService() {
    }

    public DatabaseService(GraphDatabaseService graphDB, Configuration configuration) {
        this.graphDB = graphDB;
        this.configuration = configuration;

        try (Transaction tx = graphDB.beginTx()) {
            graphDB.schema()
                    .constraintFor(new FileLabel())
                    .assertPropertyIsUnique(Fields.ID)
                    .create();
            tx.success();
        } catch (Exception exception) {
//            logger.error(exception.getMessage());
        }

        try (Transaction tx = graphDB.beginTx()) {
            graphDB.schema()
                    .constraintFor(new ChangeLabel())
                    .assertPropertyIsUnique(Fields.ID)
                    .create();
            tx.success();
        } catch (Exception exception) {
//            logger.error(exception.getMessage());
        }
    }

    @Inject
    public DatabaseService(DatabaseConfiguration dbConfig, Configuration configuration) {
        this.dbConfig = dbConfig;
        this.configuration = configuration;

        Connection object = Connection.getInstance();

        graphDB = object.getGraphDB();

        if( ! init) {
            try (Transaction tx = graphDB.beginTx()) {
                graphDB.schema()
                        .constraintFor(new FileLabel())
                        .assertPropertyIsUnique(Fields.ID)
                        .create();
                tx.success();
            } catch (Exception exception) {
//            logger.error(exception.getMessage());
            }

            try (Transaction tx = graphDB.beginTx()) {
                graphDB.schema()
                        .constraintFor(new ChangeLabel())
                        .assertPropertyIsUnique(Fields.ID)
                        .create();
                tx.success();

            } catch (Exception exception) {
//            logger.error(exception.getMessage());
            }

            init = true;
        }
    }

    /**
     * Get graphDB
     *
     * @return GraphDatabaseService
     */
    public GraphDatabaseService getGraphDB() {
        return graphDB;
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
     * Get parent for a file
     *
     * @param id String
     * @return Node
     */
    public Node getParent(String id) {
        Node resultNode = null;

        String query = "match (file {identifier: '%s'}) MATCH (folder)<-[:PARENT]-(file) return folder;";
        try (
                Transaction tx = graphDB.beginTx();
                Result result = graphDB.execute(String.format(query, id))
        ) {
            if (result.hasNext()) {
                resultNode = (Node) result.next().get("folder");
            }

            tx.success();
        } catch (Exception exception) {
            logger.error("Fail to retrieve the parent for: " + id);
            logger.error(exception.getMessage());
        }

        return resultNode;
    }

    /**
     * Get node by property
     *
     * @param value String
     * @return Node | null
     */
    public Node getNodeById(String value) {
        Node node = null;

        String query = "match (file {identifier:'%s'}) return file";

        try (Transaction tx = graphDB.beginTx()) {
            Result result = graphDB.execute(String.format(query, value));

            tx.success();

            if(result.hasNext()) {
                return (Node) result.next().get("file");
            }

        } catch (Exception exception) {
            logger.error("Fail to get node: " + value);
            logger.error(exception.getMessage());
        }

        return node;
    }

    /**
     * Get absolutePath for a node by Id
     *
     * @param node
     * @return
     * @throws Exception
     */
    public String getNodeAbsolutePath(Node node) {
        StringBuilder pathBuilder = new StringBuilder();

        String query = "match (file {identifier:'%s'}) match (file)-[r*]->(m {IsRoot:true}) return r";

        try (Transaction tx = graphDB.beginTx())
        {
            Result result = graphDB.execute(String.format(query, node.getProperty(Fields.ID).toString()));

            if ( ! result.hasNext()) {
                return "";
            }

            Map<String, Object> row = result.next();

            List<Relationship> relationshipList = (List<Relationship>) row.get("r");

            for (Relationship rel : relationshipList) {
                pathBuilder.insert(0, rel.getEndNode().getProperty(Fields.TITLE).toString());
                pathBuilder.insert(0, "/");
            }

            pathBuilder.append("/");
            pathBuilder.append(node.getProperty(Fields.TITLE).toString());

            tx.success();

        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return pathBuilder.substring(2).toString();
    }

    //@todo do not return null - throw an exception instead
    public String getMimeType(Node node) {
        try(Transaction tx = graphDB.beginTx()) {

            String type = String.valueOf(node.getProperty(Fields.MIME_TYPE));

            tx.success();

            return type;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return null;
    }

    //@todo do not return null - throw an exception instead
    public String getFileId(Node node) {
        try(Transaction tx = graphDB.beginTx()) {

            String type = String.valueOf(node.getProperty(Fields.ID));

            tx.success();

            return type;
        } catch (Exception exception) {
            logger.error(exception.getMessage());
        }

        return null;
    }

    /**
     * Update property for a specific node
     *
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
        ) {
            if (result.hasNext()) {
                resultNode = (Node) result.next().get("folder");
            }

            tx.success();

        } catch (Exception exception) {
            logger.error("Fail to update: " + id + " - " + exception.toString());

        }

        return resultNode;
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
        dbNode.setProperty(Fields.TITLE, tNode.getTitle());
        dbNode.setProperty(Fields.MIME_TYPE, tNode.getMimeType());
        dbNode.setProperty(Fields.IS_ROOT, tNode.isSuperRoot());
        dbNode.setProperty(Fields.PROCESSED, false);
        dbNode.setProperty(Fields.VERSION, tNode.getVersion());
        dbNode.setProperty(Fields.DELETED, false);

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

        childV.createRelationshipTo(parent, RelTypes.PARENT);

        if (node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }
}
