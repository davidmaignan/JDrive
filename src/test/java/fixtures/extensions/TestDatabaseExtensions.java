package fixtures.extensions;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.gson.GsonBuilder;
import configuration.Configuration;
import database.Fields;
import fixtures.deserializer.DateTimeDeserializer;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by david on 2016-05-21.
 */
public abstract class TestDatabaseExtensions implements FixturesInterface<fixtures.model.File> {
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

    @Override
    public List<fixtures.model.File> getDataSet() throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        fixtures.model.File[] fileList = gson.create().fromJson(new FileReader(
                        this.getClass().getClassLoader().getResource("fixtures/files.json").getFile()),
                fixtures.model.File[].class
        );

        return Arrays.asList(fileList);
    }

    protected File setFile(fixtures.model.File f){
        File file = new File();
        file.setId(f.id);
        file.setName(f.name);
        file.setMimeType(f.mimeType);
        file.setTrashed(f.trashed);
        file.setParents(f.parents);
        file.setVersion(f.version);
        file.setCreatedTime(f.createdTime);
        file.setModifiedTime(f.modifiedTime);

        return file;
    }
}
