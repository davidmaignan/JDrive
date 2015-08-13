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
 * David Maignan <davidmaignan@gmail.com>
 */
public class DatabaseService {
    private OrientGraph graph;
    private Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    @Inject
    public DatabaseService(Connection connection) {
        this.graph = connection.getConnection();
    }

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

    private void save(Vertex parent, TreeNode node) {
        Vertex childV = graph.addVertex(null);
        setVertex(childV, node);

        graph.addEdge(null, parent, childV, "test");

        if(node.getChildren().size() > 0) {
            for (TreeNode child : node.getChildren()) {
                save(childV, child);
            }
        }
    }

    private void setVertex(Vertex vertex, TreeNode node){
        vertex.setProperty("identifier", node.getId());
        vertex.setProperty("title", node.getTitle());
        vertex.setProperty("absolutePath", node.getAbsolutePath());
        if(node.getModifiedDate() != null) {
            vertex.setProperty("modifiedDate", node.getModifiedDate());
        }

    }

    public void debug(){
        for (Vertex v : graph.getVertices()) {
            System.out.println(v.getProperty("identifier") + " : " + v.getProperty("title") + " - " + v.getProperty("modifiedDate"));
        }
    }

    public Vertex getVertex(String id) {
        for(Vertex v : graph.getVertices("identifier", id)) {
            return v;
        }

        return  null;
    }

    public String getFullPath(Vertex vertex) {
        String path;
        Iterable<Edge> edgeList = vertex.getEdges(Direction.OUT);

        path = vertex.getProperty("title").toString();

        if (edgeList.iterator().hasNext()) {
           Vertex v = edgeList.iterator().next().getVertex(Direction.OUT);
           path = getFullPathRecursive(v) + "/" + path;
        }

        return path;
    }

    private String getFullPathRecursive(Vertex vertex) {
        String path = vertex.getProperty("title");
        Iterable<Edge> edgeList = vertex.getEdges(Direction.OUT);

        return path;
    }
}
