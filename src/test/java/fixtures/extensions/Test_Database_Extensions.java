package fixtures.extensions;

import configuration.Configuration;
import org.junit.BeforeClass;
import org.neo4j.graphdb.GraphDatabaseService;
import java.io.IOException;
import java.util.List;

/**
 * Created by david on 2016-05-21.
 */
public abstract class Test_Database_Extensions<T> {
    protected GraphDatabaseService graphDb;
    protected static Configuration configuration;

    @BeforeClass
    public static void init() throws IOException {
        configuration = new Configuration();
    }

    protected abstract List<T> getDataSet() throws IOException;
}
