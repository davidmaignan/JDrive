package org.db.neo4j;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.db.Fields;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.model.tree.TreeBuilder;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.writer.FileModule;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class DatabaseServiceTest {
    protected GraphDatabaseService graphDb;
    private DatabaseService dbService;

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
    public void testRelationShip(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");

            assertEquals(2, getResultAsList(rootNode.getRelationships()).size());

            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 3000)
    public void testGetNode(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNode(Fields.ID, "folder1"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 3000)
    public void testGetNodeById(){
        dbService.save(this.getRootNode());

        try (Transaction tx = graphDb.beginTx()) {
            assertNotNull(dbService.getNodeById("folder1"));
            tx.success();
        } catch (Exception exception) {

        }
    }

    @Test(timeout = 3000)
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

    @Test(timeout = 30000)
    public void testDeleteNode(){
        dbService.save(this.getRootNode());

        dbService.delete("folder1");

        try (Transaction tx = graphDb.beginTx()) {
            GlobalGraphOperations globalGraphOp = GlobalGraphOperations.at(graphDb);

            //2 nodes get deleted (folder 1, file 2)
            assertEquals(3, getResultAsList(globalGraphOp.getAllNodes()).size());

            //relationship between root and folder 1 is deleted
            Node rootNode = graphDb.findNode(DynamicLabel.label("File"), Fields.ID, "root");
            assertEquals(1, getResultAsList(rootNode.getRelationships()).size());

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