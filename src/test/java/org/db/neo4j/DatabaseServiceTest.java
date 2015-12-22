package org.db.neo4j;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.configuration.Configuration;
import org.db.Fields;
import org.junit.*;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeNode;
import org.model.types.MimeType;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.writer.FileModule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DatabaseServiceTest {
    protected GraphDatabaseService graphDb;
    private DatabaseService dbService;
    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        dbService = new DatabaseService(graphDb, configuration);
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test(timeout = 10000)
    public void testSave() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            assertEquals(7, getResultAsList(globalGraphOp.getAllNodes()).size());

        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testRelationShipRoot() {
        dbService.save(this.getRootNode());

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
        dbService.save(this.getRootNode());

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
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node file1 = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1");

            List<Relationship> list = getResultAsList(file1.getRelationships(RelTypes.PARENT));

            assertEquals(1, list.size());
            assertRelation(list.get(0), "file1", "folder1");
        }
    }

    @Test(timeout = 10000)
    public void testGetPropertyById() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertEquals("/Test/Path/JDrive/folder1", dbService.getNodePropertyById("folder1", Fields.PATH));
            assertEquals("folder1", dbService.getNodePropertyById("folder1", Fields.ID));
            assertEquals("folder1", dbService.getNodePropertyById("folder1", Fields.TITLE));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetNode() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNode(Fields.ID, "folder1"));
            assertNotNull(dbService.getNode(Fields.TITLE, "folder1"));
            assertNotNull(dbService.getNode(Fields.PATH, "/Test/Path/JDrive/folder1"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetNodeById() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNodeById("folder1"));
            assertNull(dbService.getNodeById("notExistingNode"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testSetNodeProperties() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1");

            assertNotNull(node);
            assertEquals("folder1", node.getProperty(Fields.ID));
            assertEquals("application/vnd.google-apps.folder", node.getProperty(Fields.MIME_TYPE));
            assertEquals("/Test/Path/JDrive/folder1", node.getProperty(Fields.PATH));
            assertEquals(1420643650751L, node.getProperty(Fields.CREATED_DATE));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 100000)
    public void testDeleteNode() {
        dbService.save(this.getRootNode());
        dbService.delete("folder2");

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            //2 nodes get deleted (folder 1, file 2)
            assertEquals(3, getResultAsList(globalGraphOp.getAllNodes()).size());
            assertEquals(2, getResultAsList(globalGraphOp.getAllRelationships()).size());

            //relationship between root and folder 1 is deleted (child and parent)
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");
            assertEquals(1, getResultAsList(rootNode.getRelationships()).size());

            assertNotNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1"));
            assertNotNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1"));

            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder2"));
            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file2"));

            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder3"));
            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file3"));

            tx.success();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Test(timeout = 10000)
    public void testGetParent() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = dbService.getParent("folder1");
            assertEquals("root", parentNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 100000)
    public void testNodeAbsolutePath() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertEquals("/Test/Path/JDrive/folder1/file1", dbService.getNodeAbsolutePath("file1"));
            assertEquals("/Test/Path/JDrive/folder2/folder3/file3", dbService.getNodeAbsolutePath("file3"));
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 100000)
    public void testUpdateProperty() {
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = dbService.update("folder1", Fields.CREATED_DATE, "999");
            assertEquals("999", parentNode.getProperty(Fields.CREATED_DATE));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testUpdateChangeFile() {
        dbService.save(this.getRootNode());

        Change change = new Change();
        change.setFileId("file1");
        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("file1");
        file1.setMimeType("application/vnd.google-apps.document");

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));
        change.setFile(file1);

        dbService.update(change);

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1");

            assertEquals("/Test/Path/JDrive/folder2/file1", node.getProperty(Fields.PATH));

            List<Relationship> result = getResultAsList(node.getRelationships(RelTypes.PARENT, Direction.OUTGOING));

            for (Relationship rel : result) {
                logger.info(rel.getEndNode().getProperty(Fields.ID).toString() + " - " + rel.getStartNode().getProperty(Fields.ID).toString());
            }
        }
    }

    @Test(timeout = 10000)
    public void testUpdateChangeFolder() {
        dbService.save(this.getRootNode());

        Change change = new Change();
        change.setFileId("folder1");
        File file1 = new File();
        file1.setTitle("folder1");
        file1.setId("folder1");
        file1.setMimeType(MimeType.FOLDER);

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));
        change.setFile(file1);

        dbService.update(change);

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1");

            assertEquals("/Test/Path/JDrive/folder2/folder1", node.getProperty(Fields.PATH));

            List<Relationship> result = getResultAsList(node.getRelationships(RelTypes.PARENT, Direction.OUTGOING));

            assertEquals(1, result.size());
            assertEquals("folder2", result.get(0).getEndNode().getProperty(Fields.ID));
            assertEquals("folder1", result.get(0).getStartNode().getProperty(Fields.ID));

            Node parentNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder2");

//            List<Relationship> resultParent = getResultAsList(parentNode.getRelationships(RelTypes.CHILD, Direction.OUTGOING));
//
//            assertEquals(2, resultParent.size());
//
//            ArrayList<String> ids = new ArrayList<>(Arrays.asList(new String[]{"file2", "folder1"}));
//
//            for(Relationship rel : resultParent) {
//                String id = rel.getEndNode().getProperty(Fields.ID).toString();
//                assertNotNull(ids.remove(id));
//            }
//
//            assertEquals(0, ids.size());
        }
    }

    @Test(timeout = 10000)
    public void testSaveNodeChange() {
        dbService.save(this.getRootNode());

        Change change = new Change();
        File file = new File();
        file.setTitle("file999");
        file.setId("file999");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setModifiedDate(new DateTime("2015-01-07T15:14:10.751Z"));

        file.setParents(this.getParentReferenceList(
                "folder1",
                false
        ));

        file.setOwners(this.getOwnerList("David Maignan", true));
        change.setFile(file);

        try (Transaction tx = graphDb.beginTx()) {
            boolean result = dbService.save(change);
            assertTrue(result);

            Node node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file999");

            assertEquals("/Test/Path/JDrive/folder1/file999", node.getProperty(Fields.PATH));

            //relationship between root and folder 1 is deleted
            Node folder1Node = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1");
//            assertEquals(3, getResultAsList(folder1Node.getRelationships(RelTypes.CHILD)).size());

            tx.success();
        } catch (Exception exception) {

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

        folder1.setParents(this.getParentReferenceList(
                "root", true
        ));

        folder1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder1);

        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("file1");
        file1.setMimeType("application/vnd.google-apps.document");

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

        folder2.setParents(this.getParentReferenceList(
                "root", false
        ));

        folder2.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder2);

        File file2 = new File();
        file2.setTitle("file2");
        file2.setId("file2");
        file2.setMimeType("application/vnd.google-apps.document");

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

        folder3.setParents(this.getParentReferenceList(
                "folder2", false
        ));

        folder3.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder3);

        File file3 = new File();
        file3.setTitle("file3");
        file3.setId("file3");
        file3.setMimeType("application/vnd.google-apps.document");

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
}