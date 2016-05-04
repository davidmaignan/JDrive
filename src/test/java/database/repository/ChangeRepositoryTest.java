package database.repository;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
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
import model.types.MimeType;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.writer.FileModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import static org.junit.Assert.*;

/**
 * Created by david on 2015-12-30.
 */
public class ChangeRepositoryTest {

    protected GraphDatabaseService graphDb;
    private ChangeRepository repository;
    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        repository = new ChangeRepository(graphDb, configuration);
    }

    @After
    public void tearDown() throws Exception {
        graphDb.shutdown();
    }

    @Test(timeout = 1000)
    public void testUpdate(){
        repository.save(this.getRootNode());

        Change change = this.generateChange(1l, "folder1");

        repository.addChange(change);

        try{
            change.setDeleted(true);
            assertTrue(repository.update(change));

            Node node = repository.getChangeById(1l);

            assertTrue((Boolean) node.getProperty(Fields.PROCESSED));
            assertTrue((Boolean) node.getProperty(Fields.DELETED));

        }catch (Exception exception){

        }
    }

    @Test(timeout = 10000)
    public void testGetProperties(){
        repository.save(this.getRootNode());

        Change change = this.generateChange(1l, "folder1");

        repository.addChange(change);

        try{
            Node node = repository.getChangeById(1l);

            assertFalse(repository.getProcessed(node));
            assertFalse(repository.getDeleted(node));
            assertEquals("folder1", repository.getFileId(node));
            assertEquals(new Long(0l), repository.getVersion(node));

        }catch (Exception exception){

        }
    }

    @Test(timeout = 10000)
    public void testUnprocessed(){
        repository.save(this.getRootNode());

        repository.addChange(this.generateChange(1l, "folder1"));
        repository.addChange(this.generateChange(2l, "folder1"));
        repository.addChange(this.generateChange(3l, "folder1"));
        repository.addChange(this.generateChange(4l, "file1"));
        repository.addChange(this.generateChange(5l, "folder2"));
        repository.addChange(this.generateChange(6l, "file3"));

        Queue<Node> result = repository.getUnprocessed();

        assertEquals(6, result.size());

        try(Transaction tx = graphDb.beginTx()) {
            Node node = result.remove();
            assertEquals(1l, node.getProperty(Fields.ID));
            assertEquals("folder1", node.getProperty(Fields.FILE_ID));

            node = result.remove();
            assertEquals(2l, node.getProperty(Fields.ID));
            assertEquals("folder1", node.getProperty(Fields.FILE_ID));

            node = result.remove();
            assertEquals(3l, node.getProperty(Fields.ID));
            assertEquals("folder1", node.getProperty(Fields.FILE_ID));

            node = result.remove();
            assertEquals(4l, node.getProperty(Fields.ID));
            assertEquals("file1", node.getProperty(Fields.FILE_ID));

            node = result.remove();
            assertEquals(5l, node.getProperty(Fields.ID));
            assertEquals("folder2", node.getProperty(Fields.FILE_ID));

            node = result.remove();
            assertEquals(6l, node.getProperty(Fields.ID));
            assertEquals("file3", node.getProperty(Fields.FILE_ID));
        }
    }

    @Test(timeout = 1000)
    public void testGetLastChangeId(){
        repository.save(this.getRootNode());

        repository.addChange(this.generateChange(1l, "folder1"));
        repository.addChange(this.generateChange(2l, "folder1"));
        repository.addChange(this.generateChange(3l, "folder1"));
        repository.addChange(this.generateChange(4l, "file1"));
        repository.addChange(this.generateChange(5l, "folder2"));
        repository.addChange(this.generateChange(6l, "file3"));

        long result = repository.getLastChangeId();

        assertEquals(6l, result);
    }

    private Change generateChange(long id, String fileId) {
        Change change = new Change();
        change.setId(id);
        change.setModificationDate(new DateTime(1L));
        change.setDeleted(false);
        change.setSelfLink("mockSelfLink");
        change.setFileId(fileId);

        File file1 = new File();
        file1.setTitle(fileId);
        file1.setId(fileId);
        file1.setMimeType(MimeType.FOLDER);
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        change.setFile(file1);

        return change;
    }

    @Test(timeout = 10000)
    public void testAddFirstChangeFile() {
        repository.save(this.getRootNode());

        Change change = new Change();
        change.setId(123456789L);
        change.setModificationDate(new DateTime(1L));
        change.setDeleted(false);
        change.setSelfLink("mockSelfLink");
        change.setFileId("folder1");

        File file1 = new File();
        file1.setTitle("folder1");
        file1.setId("folder1");
        file1.setMimeType(MimeType.FOLDER);
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        change.setFile(file1);

        repository.addChange(change);

        try (Transaction tx = graphDb.beginTx()) {
            Node node = repository.getNodeById(change.getFileId());

            Relationship relationship = node.getSingleRelationship(RelTypes.CHANGE, Direction.INCOMING);
            assertNotNull(relationship);

            assertEquals(123456789L, relationship.getStartNode().getProperty(Fields.ID));

            tx.success();
        }
    }

    @Test(timeout = 10000)
    public void testAddSecondChangeFile() {
        repository.save(this.getRootNode());

        Change change = new Change();
        change.setId(111111111L);
        change.setModificationDate(new DateTime(1L));
        change.setDeleted(false);
        change.setSelfLink("mockSelfLink");
        change.setFileId("folder1");

        File file1 = new File();
        file1.setTitle("folder1");
        file1.setId("folder1");
        file1.setMimeType(MimeType.FOLDER);
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        change.setFile(file1);

        repository.addChange(change);

        Change change2 = new Change();
        change2.setId(999999999L);
        change2.setModificationDate(new DateTime(1L));
        change2.setDeleted(false);
        change2.setSelfLink("mockSelfLink");
        change2.setFileId("folder1");

        change2.setFile(file1);

        repository.addChange(change2);

        try (Transaction tx = graphDb.beginTx()) {

            Node fileNode = repository.getNodeById("folder1");

            Relationship relationship = fileNode.getSingleRelationship(RelTypes.CHANGE, Direction.INCOMING);

            assertNotNull(relationship);
            assertEquals(111111111L, relationship.getStartNode().getProperty(Fields.ID));
            assertEquals("folder1", relationship.getEndNode().getProperty(Fields.ID));

            Node change1 = relationship.getStartNode();

            Relationship relationship2 = change1.getSingleRelationship(RelTypes.CHANGE, Direction.INCOMING);

            assertNotNull(relationship2);
            assertEquals(999999999L, relationship2.getStartNode().getProperty(Fields.ID));
            assertEquals(111111111L, relationship2.getEndNode().getProperty(Fields.ID));

            tx.success();
        }
    }

    @Test(timeout = 10000)
    public void testAddChangeDuplication() {
        repository.save(this.getRootNode());

        Change change = new Change();
        change.setId(123456789L);
        change.setModificationDate(new DateTime(1L));
        change.setDeleted(false);
        change.setSelfLink("mockSelfLink");
        change.setFileId("folder1");

        File file1 = new File();
        file1.setTitle("folder1");
        file1.setId("folder1");
        file1.setMimeType(MimeType.FOLDER);
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "folder2",
                false
        ));

        change.setFile(file1);

        assertTrue(repository.addChange(change));

        assertFalse(repository.addChange(change));

        try (Transaction tx = graphDb.beginTx()) {
            Node node = repository.getNodeById(change.getFileId());

            Relationship relationship = node.getSingleRelationship(RelTypes.CHANGE, Direction.INCOMING);
            assertNotNull(relationship);

            assertEquals(123456789L, relationship.getStartNode().getProperty(Fields.ID));

            tx.success();
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

}