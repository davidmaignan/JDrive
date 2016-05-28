package database.repository;

import com.google.api.services.drive.model.File;
import configuration.Configuration;
import database.Fields;
import database.labels.FileLabel;
import fixtures.extensions.TestDatabaseExtensions;
import model.tree.TreeBuilder;
import model.types.MimeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseServiceTest extends TestDatabaseExtensions{
    private static Logger logger = LoggerFactory.getLogger(DatabaseServiceTest.class.getSimpleName());

    private String rootId = "0AHmMPOF_fWirUk9PVA";
    private String first = "0B3mMPOF_fWirWlhxRXlfMmlPSmM";
    private String folder = "0B3mMPOF_fWircUNVWDZKb1Q3Slk";
    private String c3p0 = "0B3mMPOF_fWirS2lwd2g2b0dRTkU";

    private DatabaseService repository;

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        repository = new DatabaseService(graphDb, configuration);

        TreeBuilder treeBuilder = new TreeBuilder(rootId);
        List<File> list = new ArrayList<>();

        for(fixtures.model.File file : getDataSet()){
            list.add(setFile(file));
        }

        treeBuilder.build(list);

        repository.save(treeBuilder.getRoot());
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test(timeout = 10000)
    public void testSave() {
        try (Transaction tx = graphDb.beginTx()) {
            assertEquals(14, getResultAsList(graphDb.getAllNodes()).size());
            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testGetNodeById(){
        assertNotNull(repository.getNodeById(folder));
        assertNull(repository.getNodeById("idNotExists"));
    }


    @Test(timeout = 10000)
    public void testGetParent() {
        Node parentNode = repository.getParent(first);
        try (Transaction tx = graphDb.beginTx()) {

            assertEquals(folder, parentNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 100000)
    public void testNodeAbsolutePath() {
        try (Transaction tx = graphDb.beginTx()) {
            Node node = repository.getNodeById(first);
            Node node1 = repository.getNodeById(c3p0);
            Node node2 = repository.getNodeById("0B3mMPOF_fWirU2tqQU5PTjdWd3c");

            assertEquals("folder/first", repository.getNodeAbsolutePath(node));
            assertEquals("folder/c3po.jpg", repository.getNodeAbsolutePath(node1));
            assertEquals("folder/first/destination.csv", repository.getNodeAbsolutePath(node2));
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 100000)
    public void testNodeAbsolutePathRootNode() {
        try (Transaction tx = graphDb.beginTx()) {
            Node node = repository.getNodeById(rootId);
            assertEquals("", repository.getNodeAbsolutePath(node));
        } catch (Exception exception) {
            fail();
        }
    }

    @Test()
    public void testNodeAbsolutePathRoot(){
        try (Transaction tx = graphDb.beginTx()) {
            Node node = repository.getNodeById(rootId);
            assertEquals("", repository.getNodeAbsolutePath(node));
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 100000)
    public void testUpdateProperty() {
        repository.update(folder, Fields.CREATED_DATE, "999");

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(new FileLabel(), Fields.ID, folder);
            assertEquals(999L, node.getProperty(Fields.CREATED_DATE));

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 100000)
    public void testGetMimeType() {
        Node folder1 = repository.getNodeById(folder);
        assertEquals(MimeType.FOLDER, repository.getMimeType(folder1));
    }

    @Test(timeout = 100000)
    public void testGetMimeTypeFails() {
        Node folder1 = repository.getNodeById("not exits");
        assertNull(repository.getMimeType(folder1));
    }

    @Test(timeout = 100000)
    public void testGetFileId() {
        Node folder1 = repository.getNodeById(first);
        assertEquals(first, repository.getFileId(folder1));
    }

    @Test(timeout = 100000)
    public void testGetFileIdFails() {
        Node folder1 = repository.getNodeById("not exists");
        assertNull(repository.getFileId(folder1));
    }

    @Test(timeout = 100000)
    public void testUpdatePropertyFails() {
        assertNull(repository.update("folder1", "not exists", "999"));
    }
}