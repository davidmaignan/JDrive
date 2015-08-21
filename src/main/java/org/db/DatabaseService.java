package org.db;

import com.google.api.services.drive.model.ParentReference;
import com.google.inject.Inject;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientEdge;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientVertexType;
import org.model.tree.TreeNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Database service
 * <p>
 * Create / save vertex
 * <p>
 * A vertex is equivalent to a node in the file directory structure
 * on Google drive
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class DatabaseService {
    private OrientGraph graph;
    private Connection connection;
//    private Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Inject
    public DatabaseService(Connection connection) {
        this.connection = connection;
    }

    /**
     * Get graph
     *
     * @return OrientGraph
     */
    public OrientGraph getGraph() {
        if (this.graph == null) {
            this.graph = connection.getConnection();
        }

        return this.graph;
    }

    /**
     * Create schema
     */
    public void createSchema() {
        ODatabaseDocumentTx db = new ODatabaseDocumentTx(connection.getDBPath()).create();
//        OClass V = db.getMetadata().getSchema().getClass("V");
//        OClass treeNode = db.getMetadata().getSchema().createClass("TreeNode", V);
//
//        treeNode.createProperty(Fields.ID, OType.STRING);
//        treeNode.createProperty(Fields.TITLE, OType.STRING);
//        treeNode.createProperty(Fields.MIME_TYPE, OType.STRING);
//        treeNode.createProperty(Fields.CREATED_DATE, OType.LONG).setNotNull(false);
//        treeNode.createProperty(Fields.MODIFIED_DATE, OType.LONG).setNotNull(false);
//        treeNode.createProperty(Fields.PATH, OType.STRING);
//        treeNode.createProperty(Fields.PARENTS, OType.EMBEDDEDSET);
//        treeNode.createProperty(Fields.IS_TRASHED, OType.BOOLEAN).setMin("0");
//
//        treeNode.createIndex("IDIdx", OClass.INDEX_TYPE.UNIQUE, Fields.ID);
//
//        OClass parentType =  db.getMetadata().getSchema().createClass("Parent", V);
//        parentType.createProperty(Fields.ID, OType.STRING);
//        parentType.createProperty(Fields.IS_ROOT, OType.BOOLEAN);
//
//        db.commit();
    }

    /**
     * Save a tree of nodes in the db
     *
     * @param node
     */
    public void save(TreeNode node) {

        try {
            Vertex vertex = this.getGraph().addVertex(null);
            this.setVertex(vertex, node);

            if (node.getChildren().size() > 0) {
                for (TreeNode child : node.getChildren()) {
                    save(vertex, child);
                }
            }

            this.getGraph().commit();

        } catch (Exception exception) {
            exception.printStackTrace();
            this.getGraph().rollback();
        }
    }

    /**
     * Delete a vertex
     *
     * @param vertex Vertex
     */
    public void delete(Vertex vertex) {
        Iterable<Edge> edgeList = vertex.getEdges(Direction.OUT);

        for (Edge edge : edgeList) {
            Vertex vertex1 = edge.getVertex(Direction.IN);
            delete(vertex1);
            this.getGraph().removeEdge(edge);
        }
        this.getGraph().removeVertex(vertex);
    }

    /**
     * Update vertex properties.
     * <p>
     * //@todo check if better way to update properties for a vertex
     *
     * @param vertex
     */
    public void save(Vertex vertex) {
        this.getGraph().addVertex(vertex);
        this.getGraph().commit();
    }

    /**
     * Update a vertex property
     *
     * @param vertex   Vertex
     * @param property String
     * @param value    Object
     */
    public void updateProperty(Vertex vertex, String property, Object value) {
        vertex.removeProperty(property);
        vertex.setProperty(property, value);
        this.save(vertex);
    }

    /**
     * Debug method
     */
    public void debug() {
        for (Vertex v : this.getGraph().getVertices()) {
            System.out.println(v.getProperty("identifier") + " : " + v.getProperty("title") + " - " + v.getProperty("modifiedDate"));
        }
    }

    /**
     * Get vertex by identifier
     *
     * @param id String
     * @return Vertex|null
     */
    public Vertex getVertex(String id) {
        for (Vertex v : this.getGraph().getVertices(Fields.ID, id)) {
            return v;
        }

        return null;
    }

    /**
     * Recursively save a tree of nodes
     *
     * @param parent Vertex
     * @param node   TreeNode
     */
    private void save(Vertex parent, TreeNode node) {
        Vertex childV = this.getGraph().addVertex(null);
        setVertex(childV, node);

        parent.addEdge("child", childV);

        if (node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }

    /**
     * Set vertex properties
     *
     * @param vertex Vertex
     * @param node   TreeNode
     */
    private void setVertex(Vertex vertex, TreeNode node) {
        vertex.setProperty(Fields.ID, node.getId());
        vertex.setProperty(Fields.TITLE, node.getTitle());
        vertex.setProperty(Fields.PATH, node.getAbsolutePath());
        vertex.setProperty(Fields.MIME_TYPE, node.getMimeType());

        if (node.getCreatedDate() != null) {
            vertex.setProperty(Fields.CREATED_DATE, node.getCreatedDate().getValue());
        }

        if (node.getModifiedDate() != null) {
            vertex.setProperty(Fields.MODIFIED_DATE, node.getModifiedDate().getValue());
        }

        Set<Vertex> parentSet = new HashSet<>();

        if (node.getData() != null && node.getData().getParents() != null) {
            for (ParentReference parentReference : node.getData().getParents()) {
                Vertex parentV = this.getGraph().addVertex(null);
                parentV.setProperty(Fields.ID, parentReference.getId());
                parentV.setProperty(Fields.IS_ROOT, parentReference.getIsRoot());
                parentSet.add(parentV);
            }
        }

        vertex.setProperty(Fields.PARENTS, parentSet);
    }
}
