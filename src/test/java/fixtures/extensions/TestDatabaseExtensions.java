package fixtures.extensions;

import configuration.Configuration;
import database.Fields;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by david on 2016-05-21.
 */
public abstract class TestDatabaseExtensions implements FixturesInterface{
    protected GraphDatabaseService graphDb;
    protected static Configuration configuration;

    @BeforeClass
    public static void init() throws IOException {
        configuration = new Configuration();
    }

    /**
     * Get a list from an iterable
     *
     * @param iterable
     * @param <E>
     * @return
     */
    protected  <E> List<E> getResultAsList(Iterable<E> iterable) {
        List<E> result = new ArrayList<E>();

        iterable.forEach(s -> {
            result.add(s);
        });

        return result;
    }

    protected void assertRelation(Relationship relation, String startNode, String endNode) {
        assertEquals(startNode, relation.getStartNode().getProperty(Fields.ID).toString());
        assertEquals(endNode, relation.getEndNode().getProperty(Fields.ID).toString());
    }

    protected void debugDb(){
        List<Node> nodeList = getResultAsList(graphDb.getAllNodes());

        for(Node node : nodeList) {
            System.out.printf("%s\n", node.getProperty(Fields.ID));
        }

        List<Relationship> relationshipList = getResultAsList(graphDb.getAllRelationships());

        for (Relationship rel : relationshipList) {
            System.out.printf("Type: %s - Start: %s - End :%s\n", rel.getType(), rel.getStartNode(), rel.getEndNode());
        }
    }
}
