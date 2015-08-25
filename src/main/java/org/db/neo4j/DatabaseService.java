package org.db.neo4j;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.db.DatabaseConfiguration;
import org.db.Fields;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;


/**
 * Graph database service - Implementation for Neo4j
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DatabaseService {
    private DatabaseConfiguration dbConfig;
    private GraphDatabaseService graphDB;

    public DatabaseService(){

    }

    public DatabaseService(GraphDatabaseService graphDB) {
        this.graphDB = graphDB;
    }

    @Inject
    public DatabaseService(DatabaseConfiguration dbConfig){
        this.dbConfig = dbConfig;

        graphDB = new GraphDatabaseFactory().newEmbeddedDatabase(dbConfig.getDBPath());

        try (Transaction tx = graphDB.beginTx()) {
            graphDB.schema()
                    .constraintFor( DynamicLabel.label( "File" ) )
                    .assertPropertyIsUnique( Fields.ID )
                    .create();
            tx.success();
        } catch (Exception exception) {

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
        }
    }

    public boolean delete(String id) {
        String query = "match (file {identifier: '%s'}) " +
                "OPTIONAL MATCH (file)-[r*]->(p) " +
                "foreach (rel in r | delete rel) " +
                "delete p " +
                "with file OPTIONAL MATCH (b)-[r2]-(file) " +
                "delete r2,file";

        try (
                Transaction tx = graphDB.beginTx();
                Result result = graphDB.execute(String.format(query, id))
            )
        {
            tx.success();

            return true;
        } catch (Exception exception) {
            return false;
        }
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
        System.out.println("do you try this code");
        Node node = null;
        try (Transaction tx = graphDB.beginTx()) {
            node = graphDB.findNode(DynamicLabel.label("File"), Fields.ID, "root");

            tx.success();
        } catch (Exception exception) {
            //@todo implement sl4j
        }

        return node;
    }

    private void setNode(Node dbNode, TreeNode tNode) {
        dbNode.addLabel(DynamicLabel.label("File"));
        dbNode.setProperty(Fields.ID, tNode.getId());
        dbNode.setProperty(Fields.TITLE, tNode.getTitle());
        dbNode.setProperty(Fields.PATH, tNode.getAbsolutePath());
        dbNode.setProperty(Fields.MIME_TYPE, tNode.getMimeType());

        if (tNode.getCreatedDate() != null) {
            dbNode.setProperty(Fields.CREATED_DATE, tNode.getCreatedDate().getValue());
        }

        if (tNode.getModifiedDate() != null) {
            dbNode.setProperty(Fields.MODIFIED_DATE, tNode.getModifiedDate().getValue());
        }
//
//        Set<Vertex> parentSet = new HashSet<>();
//
//        if (tNode.getData() != null && tNode.getData().getParents() != null) {
//            for (ParentReference parentReference : tNode.getData().getParents()) {
//
//            }
//        }
//
//        node.setProperty(Fields.PARENTS, parentSet);
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

        if (node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }
}
