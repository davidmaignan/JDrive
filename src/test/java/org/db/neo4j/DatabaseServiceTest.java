package org.db.neo4j;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.db.Fields;
import org.junit.*;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeNode;
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
    public static void init(){
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        graphDb   = new TestGraphDatabaseFactory().newImpermanentDatabase();
        dbService = new DatabaseService(graphDb);
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test
    public void testSave(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            assertEquals(5, getResultAsList(globalGraphOp.getAllNodes()).size());

        } catch (Exception exception) {

        }
    }

    @Test
    public void testRelationShipRoot(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");

            assertEquals(4, getResultAsList(rootNode.getRelationships()).size());
            assertEquals(2, getResultAsList(rootNode.getRelationships(RelTypes.CHILD)).size());
            assertEquals(2, getResultAsList(rootNode.getRelationships(RelTypes.PARENT)).size());

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test
    public void testRelationShipFolder(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder2");

//            for (Relationship rel : getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.OUTGOING))) {
//                logger.info(rel.getStartNode().getProperty(Fields.ID).toString());
//            }

            assertEquals(4, getResultAsList(rootNode.getRelationships()).size());
            assertEquals(1, getResultAsList(rootNode.getRelationships(RelTypes.CHILD, Direction.INCOMING)).size());
            assertEquals(1, getResultAsList(rootNode.getRelationships(RelTypes.CHILD, Direction.OUTGOING)).size());
            assertEquals(1, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.OUTGOING)).size());
            assertEquals(1, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test
    public void testLeaf(){
        dbService.save(this.getRootNode());

        try(Transaction tx = graphDb.beginTx()) {
            Node file1 = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1");

            List<Relationship> list = getResultAsList(file1.getRelationships(RelTypes.PARENT));

            assertEquals(1, list.size());
            assertEquals("folder1", list.get(0).getEndNode().getProperty(Fields.ID));
            assertEquals("file1", list.get(0).getStartNode().getProperty(Fields.ID));
        }
    }

    @Test
    public void testGetPropertyById(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertEquals("/Test/Path/JDrive/folder1", dbService.getNodePropertyById("folder1", Fields.PATH));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetNode(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNode(Fields.ID, "folder1"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetNodeById(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNodeById("folder1"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testSetNodeProperties(){
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
    public void testDeleteNode(){
        dbService.save(this.getRootNode());
        dbService.delete("folder1");

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            //2 nodes get deleted (folder 1, file 2)
            assertEquals(3, getResultAsList(globalGraphOp.getAllNodes()).size());

            //relationship between root and folder 1 is deleted (child and parent)
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");
            assertEquals(2, getResultAsList(rootNode.getRelationships()).size());

            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder1"));
            assertNull(graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "file1"));

            tx.success();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    @Test(timeout = 10000)
    public void testGetParent(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = dbService.getParent("folder1");
            assertEquals("root", parentNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 100000)
    public void testUpdateProperty(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = dbService.update("folder1", Fields.CREATED_DATE, "999");
            assertEquals("999", parentNode.getProperty(Fields.CREATED_DATE));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testUpdateChange(){
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

            List<Relationship> result = getResultAsList(node.getRelationships(RelTypes.PARENT));

            assertEquals(1, result.size());
            assertEquals("folder2", result.get(0).getEndNode().getProperty(Fields.ID));
            assertEquals("file1", result.get(0).getStartNode().getProperty(Fields.ID));

            Node parentNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "folder2");

            List<Relationship> resultParent = getResultAsList(parentNode.getRelationships(RelTypes.CHILD, Direction.OUTGOING));
            assertEquals(2, resultParent.size());

            ArrayList<String> ids = new ArrayList<>(Arrays.asList(new String[]{"file1", "file2"}));

            for(Relationship rel : resultParent) {
                String id = rel.getEndNode().getProperty(Fields.ID).toString();
                assertNotNull(ids.remove(id));
            }

            assertEquals(0, ids.size());
        }
    }

    @Test(timeout = 10000)
    @Ignore
    public void testSaveNodeChange(){
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
            assertEquals(3, getResultAsList(folder1Node.getRelationships(RelTypes.CHILD)).size());

            tx.success();
        } catch (Exception exception) {

        }
    }

    /**
     * Get a list from an iterable
     * @param iterable
     * @param <E>
     * @return
     */
    private <E> List<E> getResultAsList(Iterable<E> iterable){
        List<E> result = new ArrayList<E>();

        iterable.forEach( s -> {
            result.add(s);
        });

        return result;
    }

    /**
     *  - root
     *     - folder 1
     *         - file1
     *     - folder 2
     *         - file2
     */
    private TreeNode getRootNode(){
        ArrayList<File>listFile = new ArrayList<>();

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

        Injector injector = Guice.createInjector(new FileModule());
        TreeBuilder treeBuilder = injector.getInstance(TreeBuilder.class);

        treeBuilder.build(listFile);

        return treeBuilder.getRoot();
    }

    private ArrayList<ParentReference> getParentReferenceList(String id, boolean bool){
        ArrayList<ParentReference> parentList = new ArrayList<>();
        parentList.add(this.getParentReference(id, bool));

        return parentList;
    }

    private ParentReference getParentReference(String id, boolean bool){
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
}