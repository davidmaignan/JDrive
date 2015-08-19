package org.db;

import com.google.inject.Inject;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import org.model.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Database service
 *
 * Create and save vertex corresponding to a node from the google drive
 * in Orient DB
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
     * Save a tree of nodes in the db
     *
     * @param node
     */
    public void save(TreeNode node) {
        try {
            Vertex vertex = graph.addVertex(null);
            this.setVertex(vertex, node);

            if(node.getChildren().size() > 0) {
                for(TreeNode child : node.getChildren()) {
                    save(vertex, child);
                }
            }

            graph.commit();

        } catch (Exception exception) {
            System.out.println(exception);
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
        Vertex childV = graph.addVertex(null);
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
        vertex.setProperty(Fields.FILE, node.getData());
        vertex.setProperty(Fields.MIME_TYPE, node.getMimeType());

        if(node.getModifiedDate() != null) {
            vertex.setProperty(Fields.MODIFIED_DATE, node.getModifiedDate());
        }
    }

    /**
     * Debug method
     */
    public void debug(){
        for (Vertex v : graph.getVertices()) {
            //System.out.println(v.getProperty("identifier") + " : " + v.getProperty("title") + " - " + v.getProperty("modifiedDate"));
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
