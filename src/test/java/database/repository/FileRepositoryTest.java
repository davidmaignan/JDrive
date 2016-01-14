package database.repository;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.Fields;
import database.RelTypes;
import org.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.writer.FileModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

/**
 * Created by david on 2015-12-31.
 */
public class FileRepositoryTest {
    protected GraphDatabaseService graphDb;
    private FileRepository fileRepository;
    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        fileRepository = new FileRepository(graphDb, configuration);
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test(timeout = 100000)
    public void testGetUnProcessedFile(){
        fileRepository.save(this.getRootNode());

        Queue<Node> result = fileRepository.getUnprocessedQueue();

        assertEquals(7, result.size());

        try(Transaction tx = graphDb.beginTx()) {
            assertEquals("root", result.remove().getProperty(Fields.ID));
            assertEquals("folder1", result.remove().getProperty(Fields.ID));
            assertEquals("file1", result.remove().getProperty(Fields.ID));
            assertEquals("folder2", result.remove().getProperty(Fields.ID));
            assertEquals("file2", result.remove().getProperty(Fields.ID));
            assertEquals("folder3", result.remove().getProperty(Fields.ID));
            assertEquals("file3", result.remove().getProperty(Fields.ID));
        }
    }

    @Test(timeout = 100000)
    public void testMarkFileAsProcessed(){
        fileRepository.save(this.getRootNode());

        assertTrue(fileRepository.markAsProcessed("file1"));
    }

    @Test(timeout = 10000)
    public void testSave() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            assertEquals(7, getResultAsList(globalGraphOp.getAllNodes()).size());

            tx.success();

        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testRelationShipRoot() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");

            assertEquals(2, getResultAsList(rootNode.getRelationships()).size());
            assertEquals(0, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.OUTGOING)).size());

            List<Relationship> relationships = getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING));
            assertEquals(2, relationships.size());

            List<String> nodeIds = new ArrayList<>(Arrays.asList(new String[]{"folder1", "folder2"}));

            for (Relationship rel : relationships) {
                assertEquals(rootNode, rel.getEndNode());
                String nodeID = rel.getStartNode().getProperty(Fields.ID).toString();
                assertTrue(nodeIds.remove(nodeID));
            }

            assertEquals(0, nodeIds.size());

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testRelationShipFolder() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder3");

            assertEquals(2, getResultAsList(rootNode.getRelationships()).size());

            List<Relationship> incomingRelationshipList = getResultAsList(
                    rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );
            assertEquals(1, incomingRelationshipList.size());
            assertRelation(incomingRelationshipList.get(0), "file3", "folder3");

            List<Relationship> outgoingRelationshipList = getResultAsList(
                    rootNode.getRelationships(RelTypes.PARENT, Direction.OUTGOING)
            );

            assertEquals(1, outgoingRelationshipList.size());
            assertRelation(outgoingRelationshipList.get(0), "folder3", "folder2");

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testLeaf() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node file1 = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1");

            List<Relationship> list = getResultAsList(file1.getRelationships(RelTypes.PARENT));

            assertEquals(1, list.size());
            assertRelation(list.get(0), "file1", "folder1");
        }
    }

    @Test(timeout = 10000)
    public void testSetNodeProperties() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1");

            assertNotNull(node);
            assertEquals("folder1", node.getProperty(Fields.ID));
            assertEquals("application/vnd.google-apps.folder", node.getProperty(Fields.MIME_TYPE));
            assertEquals(1420643650751L, node.getProperty(Fields.CREATED_DATE));
            assertFalse((boolean)node.getProperty(Fields.PROCESSED));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetParent() {
        fileRepository.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = fileRepository.getParent("folder1");
            assertEquals("root", parentNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 1000)
    public void testCreateIfNotExists(){
        fail("Not yet implemented");
    }

    /**
     * - root
     * - folder 1
     * - file1
     * - folder 2
     * - file2
     */
    private TreeNode getRootNode() {
        ArrayList<File> listFile = new ArrayList<>();

        File folder1 = new File();

        folder1.setTitle("folder1");
        folder1.setId("folder1");
        folder1.setMimeType("application/vnd.google-apps.folder");
        folder1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder1.setVersion(0l);

        folder1.setParents(this.getParentReferenceList(
                "root", true
        ));

        folder1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder1);

        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("file1");
        file1.setMimeType("application/vnd.google-apps.document");
        file1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "folder1",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file1);

        File folder2 = new File();
        folder2.setTitle("folder2");
        folder2.setId("folder2");
        folder2.setMimeType("application/vnd.google-apps.folder");
        folder2.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder2.setVersion(0l);

        folder2.setParents(this.getParentReferenceList(
                "root", false
        ));

        folder2.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder2);

        File file2 = new File();
        file2.setTitle("file2");
        file2.setId("file2");
        file2.setMimeType("application/vnd.google-apps.document");
        file2.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file2.setVersion(0l);

        file2.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        file2.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file2);

        File folder3 = new File();
        folder3.setTitle("folder3");
        folder3.setId("folder3");
        folder3.setMimeType("application/vnd.google-apps.folder");
        folder3.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder3.setVersion(0l);

        folder3.setParents(this.getParentReferenceList(
                "folder2", false
        ));

        folder3.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder3);

        File file3 = new File();
        file3.setTitle("file3");
        file3.setId("file3");
        file3.setMimeType("application/vnd.google-apps.document");
        file3.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file3.setVersion(0l);

        file3.setParents(this.getParentReferenceList(
                "folder3",
                false
        ));

        file3.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file3);

        Injector injector = Guice.createInjector(new FileModule());
        TreeBuilder treeBuilder = injector.getInstance(TreeBuilder.class);

        treeBuilder.build(listFile);

        return treeBuilder.getRoot();
    }

    private ArrayList<ParentReference> getParentReferenceList(String id, boolean bool) {
        ArrayList<ParentReference> parentList = new ArrayList<>();
        parentList.add(this.getParentReference(id, bool));

        return parentList;
    }

    private ParentReference getParentReference(String id, boolean bool) {
        ParentReference parentReference = new ParentReference();
        parentReference.setId(id);
        parentReference.setIsRoot(bool);

        return parentReference;
    }

    private ArrayList<User> getOwnerList(String displayName, boolean isAuthenticatedUser) {
        ArrayList<User> ownerList = new ArrayList<>();
        ownerList.add(this.getOwner(displayName, isAuthenticatedUser));

        return ownerList;
    }

    private User getOwner(String displayName, boolean isAuthenticatedUser) {
        User owner = new User();
        owner.setDisplayName(displayName);
        owner.setIsAuthenticatedUser(isAuthenticatedUser);

        return owner;
    }

    private void assertRelation(Relationship relation, String startNode, String endNode) {
        assertEquals(startNode, relation.getStartNode().getProperty(Fields.ID).toString());
        assertEquals(endNode, relation.getEndNode().getProperty(Fields.ID).toString());
    }

    private void debugDb(){
        GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

        List<Node> nodeList = getResultAsList(globalGraphOp.getAllNodes());

        for(Node node : nodeList) {
            System.out.printf("%s\n", node.getProperty(Fields.ID));
        }

        List<Relationship> relationshipList = getResultAsList(globalGraphOp.getAllRelationships());

        for (Relationship rel : relationshipList) {
            System.out.printf("Type: %s - Start: %s - End :%s\n", rel.getType(), rel.getStartNode(), rel.getEndNode());
        }
    }

    /**
     * Get a list from an iterable
     *
     * @param iterable
     * @param <E>
     * @return
     */
    private <E> List<E> getResultAsList(Iterable<E> iterable) {
        List<E> result = new ArrayList<E>();

        iterable.forEach(s -> {
            result.add(s);
        });

        return result;
    }
}