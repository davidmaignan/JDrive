package database.repository;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import database.DatabaseModule;
import database.Fields;
import database.RelTypes;
import configuration.Configuration;
import database.labels.FileLabel;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

/**
 * Created by david on 2015-12-31.
 */
@RunWith(DataProviderRunner.class)
public class FileRepositoryTest {
    protected GraphDatabaseService graphDb;
    private FileRepository repository;
    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        repository = new FileRepository(graphDb, configuration);

        repository.save(this.getRootNode());
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test(timeout = 10000)
    public void testGetProperties(){
        try{
            Node node = repository.getNodeById("folder1");

            assertEquals(new Long(100l), repository.getVersion(node));
            assertEquals("folder1", repository.getTitle(node));
        }catch (Exception exception){

        }
    }

    @Test(timeout = 10000)
    public void testUpdateRelationship(){
        Node nodeChild = repository.getNodeById("file2");
        Node nodeParent = repository.getNodeById("folder3");

        assertTrue(repository.updateParentRelation(nodeChild, nodeParent));

        try(Transaction tx = graphDb.beginTx()) {

            Node file2 = graphDb.findNode(new FileLabel(), Fields.ID, "file2");
            Node folder3 = graphDb.findNode(new FileLabel(), Fields.ID, "folder3");

            assertEquals("folder3",
                    file2.getSingleRelationship(RelTypes.PARENT, Direction.OUTGOING)
                            .getEndNode()
                            .getProperty(Fields.ID));

            Node folder2 = graphDb.findNode(new FileLabel(), Fields.ID, "folder2");

            List<Relationship> relationshipList = getResultAsList(folder2.getRelationships(RelTypes.PARENT, Direction.INCOMING));

            assertEquals(1, relationshipList.size());
            assertEquals("folder3", relationshipList.get(0).getStartNode().getProperty(Fields.ID));
            assertEquals("folder2", relationshipList.get(0).getEndNode().getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception){

        }
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedStringArgument(){
        assertTrue(repository.markAsDeleted("folder2"));

        try(Transaction tx = graphDb.beginTx()) {
            Node folder2 = graphDb.findNode(new FileLabel(), Fields.ID, "folder2");
            Node file2 = graphDb.findNode(new FileLabel(), Fields.ID, "file2");
            Node folder3 = graphDb.findNode(new FileLabel(), Fields.ID, "folder3");
            Node file3 = graphDb.findNode(new FileLabel(), Fields.ID, "file3");

            assertTrue((boolean)folder2.getProperty(Fields.DELETED));
            assertTrue((boolean)file2.getProperty(Fields.DELETED));
            assertTrue((boolean)folder3.getProperty(Fields.DELETED));
            assertTrue((boolean)file3.getProperty(Fields.DELETED));

        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedNodeArgument(){
        try(Transaction tx = graphDb.beginTx()) {
            Node folder2 = graphDb.findNode(new FileLabel(), Fields.ID, "folder2");
            Node file2 = graphDb.findNode(new FileLabel(), Fields.ID, "file2");
            Node folder3 = graphDb.findNode(new FileLabel(), Fields.ID, "folder3");
            Node file3 = graphDb.findNode(new FileLabel(), Fields.ID, "file3");

            assertTrue(repository.markAsDeleted(folder2));

            assertTrue((boolean)folder2.getProperty(Fields.DELETED));
            assertFalse((boolean) file2.getProperty(Fields.DELETED));
            assertFalse((boolean) folder3.getProperty(Fields.DELETED));
            assertFalse((boolean) file3.getProperty(Fields.DELETED));

        } catch (Exception exception) {

        }
    }

    @Test(timeout = 100000)
    public void testGetUnProcessedFile(){
        Queue<Node> result = repository.getUnprocessedQueue();

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
        assertTrue(repository.markAsProcessed("file1"));
    }

    @Test(timeout = 10000)
    public void testSave() {
        try (Transaction tx = graphDb.beginTx()) {
            assertEquals(7, getResultAsList(graphDb.getAllNodes()).size());

            tx.success();

        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testRelationShipRoot() {
        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, "root");

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
        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, "folder3");

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
        try (Transaction tx = graphDb.beginTx()) {
            Node file1 = graphDb.findNode(new FileLabel(), Fields.ID, "file1");

            List<Relationship> list = getResultAsList(file1.getRelationships(RelTypes.PARENT));

            assertEquals(1, list.size());
            assertRelation(list.get(0), "file1", "folder1");
        }
    }

    @Test(timeout = 10000)
    public void testSetNodeProperties() {
        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(new FileLabel(), Fields.ID, "folder1");

            assertNotNull(node);
            assertEquals("folder1", node.getProperty(Fields.ID));
            assertEquals("application/vnd.google-apps.folder", node.getProperty(Fields.MIME_TYPE));
            assertEquals(1420643650751L, node.getProperty(Fields.CREATED_DATE));
            assertFalse((boolean) node.getProperty(Fields.PROCESSED));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testGetParent() {
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = repository.getParent("folder1");
            assertEquals("root", parentNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExistsFailure(){
        File file = new File();
        file.setTitle("file1");
        file.setId("file1");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);

        file.setParents(this.getParentReferenceList(
                "folder1",
                false
        ));

        file.setOwners(this.getOwnerList("David Maignan", true));

        assertFalse(repository.createIfNotExists(file));
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExistsSuccess(){
        File file = new File();
        file.setTitle("newFile");
        file.setId("newFile");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);

        file.setParents(this.getParentReferenceList(
                "folder1",
                false
        ));

        file.setOwners(this.getOwnerList("David Maignan", true));

        assertTrue(repository.createIfNotExists(file));

        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(new FileLabel(), Fields.ID, "newFile");

            Relationship relationship = node.getSingleRelationship(RelTypes.PARENT, Direction.OUTGOING);

            assertEquals("folder1", relationship.getEndNode().getProperty(Fields.ID));
            assertEquals("newFile", relationship.getStartNode().getProperty(Fields.ID));
        }
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
        folder1.setVersion(100l);

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

        Injector injector = Guice.createInjector(new DatabaseModule());
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

        List<Node> nodeList = getResultAsList(graphDb.getAllNodes());

        for(Node node : nodeList) {
            System.out.printf("%s\n", node.getProperty(Fields.ID));
        }

        List<Relationship> relationshipList = getResultAsList(graphDb.getAllRelationships());

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

    @Test
    public void testGetTitle() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node node = repository.getNodeById("folder1");
            assertEquals("folder1", repository.getTitle(node));
        }catch(Exception exception){

        }
    }

    @Test
    public void testGetVersion() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node node = repository.getNodeById("folder1");
            assertEquals(new Long(100), repository.getVersion(node));
        }catch(Exception exception){

        }
    }

    @Test
    public void testUpdateParentRelation() throws Exception {
        try(Transaction tx = graphDb.beginTx()){

            Node folder3 = repository.getNodeById("folder3");
            Node folder1 = repository.getNodeById("folder1");

            repository.updateParentRelation(folder3, folder1);

            List<Relationship> incomingRelationshipList = getResultAsList(
                    folder1.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );
            assertEquals(2, incomingRelationshipList.size());
            assertRelation(incomingRelationshipList.get(0), "file1", "folder1");
            assertRelation(incomingRelationshipList.get(1), "folder3", "folder1");


        }catch(Exception exception){

        }
    }

    @Test
    public void testMarkAsTrashed() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node folder1 = repository.getNodeById("folder1");

            repository.markAsTrashed(folder1);

            assertTrue((boolean)folder1.getProperty(Fields.TRASHED));
            assertFalse((boolean) folder1.getProperty(Fields.PROCESSED));
        }catch(Exception exception){

        }
    }

    @Test
    public void testMarkAsUnTrashed() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node folder1 = repository.getNodeById("folder1");

            assertTrue(repository.markAsUnTrashed(folder1));

            assertFalse((boolean) folder1.getProperty(Fields.TRASHED));
            assertFalse((boolean)folder1.getProperty(Fields.PROCESSED));
        }catch(Exception exception){

        }
    }

    @Test
    public void testMarkAsProcessed() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node folder1 = repository.getNodeById("folder1");

            assertTrue(repository.markAsProcessed(folder1));

            assertTrue((boolean) folder1.getProperty(Fields.PROCESSED));
        }catch(Exception exception){

        }
    }

    @Test
    public void testUpdate() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node node = repository.getNodeById("folder1");

            File folder1 = new File();

            folder1.setTitle("newTitle");
            folder1.setId("newId");
            folder1.setMimeType("application/vnd.google-apps.document");
            folder1.setCreatedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            folder1.setModifiedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            folder1.setVersion(1l);

            folder1.setParents(this.getParentReferenceList(
                    "root", true
            ));

            folder1.setOwners(this.getOwnerList("David Maignan", true));

            assertTrue(repository.update(node, folder1));

            assertEquals("newTitle", node.getProperty(Fields.TITLE));
            assertEquals(new Long(1l), node.getProperty(Fields.VERSION));
            assertEquals("application/vnd.google-apps.document", node.getProperty(Fields.MIME_TYPE));
        }catch(Exception exception){

        }
    }

    @Test
    public void testGetUnprocessedQueue() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Queue<Node> result = repository.getUnprocessedQueue();

            assertEquals(7, result.size());
        }catch (Exception exception) {

        }
    }

    @Test
    public void testGetTrashedList() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node folder1 = repository.getNodeById("folder1");
            repository.markAsTrashed(folder1);

            Node file1 = repository.getNodeById("file1");
            repository.markAsTrashed(file1);

            List<Node> result = repository.getTrashedList();

            assertEquals(2, result.size());
        }catch (Exception exception) {

        }
    }

    @Test
    public void testGetTrashedQueue() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node folder1 = repository.getNodeById("folder1");
            repository.markAsTrashed(folder1);

            Node file1 = repository.getNodeById("file1");
            repository.markAsTrashed(file1);

            Queue<Node> result = repository.getTrashedQueue();

            assertEquals(2, result.size());
        }catch (Exception exception) {

        }
    }

    @Test
    public void testGetDeletedQueue() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node folder1 = repository.getNodeById("folder1");
            repository.markAsDeleted(folder1);

            Node file1 = repository.getNodeById("file1");
            repository.markAsDeleted(file1);

            Queue<Node> result = repository.getDeletedQueue();

            assertEquals(2, result.size());
        }catch (Exception exception) {

        }
    }

    @Test
    public void testCreateIfNotExists() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            File file = new File();

            file.setTitle("newTitle");
            file.setId("newId");
            file.setMimeType("application/vnd.google-apps.document");
            file.setCreatedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setModifiedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setVersion(1l);

            file.setParents(this.getParentReferenceList(
                    "root", true
            ));

            file.setOwners(this.getOwnerList("David Maignan", true));

            assertTrue(repository.createIfNotExists(file));


        }catch (Exception exception) {

        }
    }

    @DataProvider
    public static Object[][] dataProviderCreateNode(){
        return new Object[][]{
                {false, false, false},
                {true, false, true},
                {false, true, true},
                {true, true, true}
        };
    }

//    /**
//     * Get trashed label value if available
//     *
//     * @return boolean
//     */
//    private boolean isTrash(File file){
//        return (file != null
//                && file.getExplicitlyTrashed() != null
//                && file.getExplicitlyTrashed())
//                || (file != null
//                && file.getLabels() != null
//                && file.getLabels().getTrashed());
//    }

    @Test
    @UseDataProvider("dataProviderCreateNode")
    public void testCreateNode(boolean explicitly, boolean trashed, boolean expected) throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            File file = new File();

            file.setTitle("newTitle");
            file.setId("newId");
            file.setMimeType("application/vnd.google-apps.document");
            file.setCreatedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setModifiedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setVersion(1l);

            file.setExplicitlyTrashed(explicitly);
            file.getLabels().setTrashed(trashed);

            file.setParents(this.getParentReferenceList(
                    "root", true
            ));

            file.setOwners(this.getOwnerList("David Maignan", true));

            Node result = repository.createNode(file);

            assertEquals("newTitle", result.getProperty(Fields.TITLE));
            assertEquals("newId", result.getProperty(Fields.ID));
            assertEquals(expected, (boolean) result.getProperty(Fields.TRASHED));
            assertFalse((boolean)result.getProperty(Fields.DELETED));
        }catch (Exception exception) {

        }
    }

    @Test
    public void testCreateParentRelation() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            File file = new File();

            file.setTitle("file");
            file.setId("file");
            file.setMimeType("application/vnd.google-apps.document");
            file.setCreatedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setModifiedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            file.setVersion(1l);

            file.setParents(this.getParentReferenceList(
                    "root", true
            ));

            file.setOwners(this.getOwnerList("David Maignan", true));

            Node fileNode = repository.createNode(file);

            File folder1 = new File();

            folder1.setTitle("folder");
            folder1.setId("folder");
            folder1.setMimeType("application/vnd.google-apps.folder");
            folder1.setCreatedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            folder1.setModifiedDate(new DateTime("2016-01-07T15:14:10.751Z"));
            folder1.setVersion(1l);

            folder1.setParents(this.getParentReferenceList(
                    "root", true
            ));

            folder1.setOwners(this.getOwnerList("David Maignan", true));

            Node folderNode = repository.createNode(file);

            repository.createParentRelation(fileNode, folderNode);

            List<Relationship> incomingRelationshipList = getResultAsList(
                    folderNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );
            assertEquals(1, incomingRelationshipList.size());
            assertRelation(incomingRelationshipList.get(0), "file", "folder");
        }catch (Exception exception) {

        }
    }
}