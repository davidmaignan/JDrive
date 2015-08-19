package org.db;

import com.google.api.services.drive.model.ParentReference;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.exception.OSchemaException;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.model.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Database service
 *
 * Create / save vertex
 *
 * A vertex is equivalent to a node in the file directory structure
 * on Google drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DatabaseService {
    private OrientGraph graph;
    private Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Inject
    public DatabaseService(Connection connection) {
        this.graph = connection.getConnection();
    }

    /**
     * Create a treeNode type in orientDB
     */
    public void createTreeNodeType() {
        try {
            OrientVertexType treeNode = graph.createVertexType("TreeNode");
            treeNode.createProperty(Fields.ID, OType.STRING);
            treeNode.createProperty(Fields.TITLE, OType.STRING);
            treeNode.createProperty(Fields.MIME_TYPE, OType.STRING);
            treeNode.createProperty(Fields.CREATED_DATE, OType.LONG).setNotNull(false);
            treeNode.createProperty(Fields.MODIFIED_DATE, OType.LONG).setNotNull(false);
            treeNode.createProperty(Fields.PATH, OType.STRING);
            treeNode.createProperty(Fields.PARENTS, OType.EMBEDDEDSET);
            treeNode.createProperty(Fields.IS_TRASHED, OType.BOOLEAN).setMin("0");

            treeNode.createIndex("IDIdx", OClass.INDEX_TYPE.UNIQUE, Fields.ID);
        }catch (OSchemaException exception){

        }
    }

    /**
     * Create a parentType in orientDB
     */
    public void createParentType(){
        try{
            OrientVertexType parentType = graph.createVertexType("Parent");
            parentType.createProperty(Fields.ID, OType.STRING);
            parentType.createProperty(Fields.IS_ROOT, OType.BOOLEAN);
        }catch (OSchemaException exception) {

        }
    }

    /**
     * Save a tree of nodes in the db
     *
     * @param node
     */
    public void save(TreeNode node) {
        try {
            Vertex vertex = graph.addVertex("class:TreeNode");
            this.setVertex(vertex, node);

            if(node.getChildren().size() > 0) {
                for(TreeNode child : node.getChildren()) {
                    save(vertex, child);
                }
            }

            graph.commit();

        } catch (Exception exception) {
            exception.printStackTrace();
            graph.rollback();
        }
    }

    /**
     * Update vertex properties.
     *
     * //@todo check if better way to update properties for a vertex
     *
     * @param vertex
     */
    public void save(Vertex vertex) {
        graph.removeVertex(vertex);
        graph.addVertex(vertex);
        graph.commit();
    }

    /**
     * Recursively save a tree of nodes
     *
     * @param parent Vertex
     * @param node TreeNode
     */
    private void save(Vertex parent, TreeNode node) {
        Vertex childV = graph.addVertex("class:TreeNode");
        setVertex(childV, node);

        if(node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }

    /**
     * Set vertex properties
     *
     * @param vertex Vertex
     * @param node TreeNode
     */
    private void setVertex(Vertex vertex, TreeNode node){
        vertex.setProperty(Fields.ID, node.getId());
        vertex.setProperty(Fields.TITLE, node.getTitle());
        vertex.setProperty(Fields.PATH, node.getAbsolutePath());
        vertex.setProperty(Fields.MIME_TYPE, node.getMimeType());

        if(node.getCreatedDate() != null) {
            vertex.setProperty(Fields.CREATED_DATE, node.getCreatedDate().getValue());
        }

        if(node.getModifiedDate() != null) {
            vertex.setProperty(Fields.MODIFIED_DATE, node.getModifiedDate().getValue());
        }

        Set<Vertex> parentSet = new HashSet<>();

        if(node.getData() != null && node.getData().getParents() != null ) {
            for(ParentReference parentReference : node.getData().getParents()) {
                Vertex parentV = graph.addVertex("class:Parent");
                parentV.setProperty(Fields.ID, parentReference.getId());
                parentV.setProperty(Fields.IS_ROOT, parentReference.getIsRoot());
                parentSet.add(parentV);
            }
        }

        vertex.setProperty(Fields.PARENTS, parentSet);
    }

    /**
     * Debug method
     */
    public void debug(){
        for (Vertex v : graph.getVertices()) {
            System.out.println(v.getProperty("identifier") + " : " + v.getProperty("title") + " - " + v.getProperty("modifiedDate"));
        }
    }

    /**
     * Get vertex by identifier
     *
     * @param id String
     *
     * @return Vertex|null
     */
    public Vertex getVertex(String id) {
        for(Vertex v : graph.getVertices(Fields.ID, id)) {
            return v;
        }

        return  null;
    }
}
