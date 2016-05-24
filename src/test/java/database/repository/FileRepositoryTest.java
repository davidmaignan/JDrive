package database.repository;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.ParentReference;
import com.google.gson.GsonBuilder;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import database.Fields;
import configuration.Configuration;
import database.RelTypes;
import database.labels.FileLabel;
import fixtures.deserializer.DateTimeDeserializer;
import fixtures.extensions.TestDatabaseExtensions;
import model.types.MimeType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import model.tree.TreeBuilder;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2015-12-31.
 */
@RunWith(DataProviderRunner.class)
public class FileRepositoryTest extends TestDatabaseExtensions<fixtures.model.File> {
    private FileRepository repository;
    private static Logger logger = LoggerFactory.getLogger(FileRepositoryTest.class.getSimpleName());
    private String rootId = "0AHmMPOF_fWirUk9PVA";
    private String first = "0B3mMPOF_fWirWlhxRXlfMmlPSmM";
    private String folder = "0B3mMPOF_fWircUNVWDZKb1Q3Slk";
    private String c3p0 = "0B3mMPOF_fWirS2lwd2g2b0dRTkU";

    @Before
    public void setUp() throws Exception {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        repository = new FileRepository(graphDb, configuration);

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

    @Test
    public void testCreateRootNodeFails(){
        File rootFile = new File();
        rootFile.setId(rootId);

        assertFalse(repository.createRootNode(rootFile));
    }

    @Test
    public void testCreateRootNode(){
        File rootFile = new File();
        rootFile.setId("mockRootId");

        assertTrue(repository.createRootNode(rootFile));
    }

    @Test
    public void getGetName(){
        try(Transaction tx = graphDb.beginTx()){
            Node folderNode = graphDb.findNode(new FileLabel(), Fields.ID, folder);

            assertEquals("folder", repository.getName(folderNode));
            tx.success();
        } catch (Exception exception){
            fail();
        }
    }

    @Test
    public void getGetNameFails(){
        Node folderNode = null;
        try(Transaction tx = graphDb.beginTx()){
            folderNode = graphDb.findNode(new FileLabel(), Fields.ID, "notExists");
            tx.success();
        } catch (Exception exception){
            fail();
        }

        assertNull(repository.getName(folderNode));
    }

    @Test
    public void testGetRootNode(){
        try(Transaction tx = graphDb.beginTx()){
            Node node = repository.getRootNode();

            Map<String, Object> propertyList = node.getProperties(
                    Fields.IS_ROOT,
                    Fields.NAME,
                    Fields.MIME_TYPE
            );

            assertEquals(rootId, node.getProperty(Fields.ID));
            assertTrue((boolean)propertyList.get(Fields.IS_ROOT));
            assertEquals("", propertyList.get(Fields.NAME));
            assertEquals(MimeType.FOLDER, propertyList.get(Fields.MIME_TYPE));
            tx.success();
        }catch (Exception exception){
            fail();
        }
    }

    @Test
    public void testGetNodeById(){
        assertNotNull(repository.getNodeById("0B3mMPOF_fWirUFMyeDR0ckI4WHM"));
        assertNull(repository.getNodeById("idNotExists"));
    }

    @Test(timeout = 10000)
    public void testNodePropertiesSetFromTreeNode(){
        Node node = repository.getNodeById("0B3mMPOF_fWirUFMyeDR0ckI4WHM");

        try(Transaction tx = graphDb.beginTx()){
            assertEquals("0B3mMPOF_fWirUFMyeDR0ckI4WHM", node.getProperty(Fields.ID));
            assertEquals("folder1", node.getProperty(Fields.NAME));
            assertEquals(18551L, node.getProperty(Fields.VERSION));
            assertEquals(MimeType.FOLDER, node.getProperty(Fields.MIME_TYPE));
            assertFalse((boolean)node.getProperty(Fields.TRASHED));
            assertEquals(1462907068438L, node.getProperty(Fields.CREATED_DATE));
            assertEquals(1463504088341L, node.getProperty(Fields.MODIFIED_DATE));
            tx.success();
        }catch (Exception exception){
            fail();
        }
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

    @Test(timeout = 10000)
    public void testRelationShipRoot() {
        try (Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(13, getResultAsList(graphDb.getAllRelationships()).size());
            assertEquals(0, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.OUTGOING)).size());

            List<Relationship> relationships = getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING));
            assertEquals(5, relationships.size());

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testRelationShipFolder() {
        try (Transaction tx = graphDb.beginTx()) {
            Node node = graphDb.findNode(new FileLabel(), Fields.ID, first);

            assertEquals(4, getResultAsList(node.getRelationships()).size());

            List<Relationship> incomingRelationshipList = getResultAsList(
                    node.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );

            ArrayList<String> expected = new ArrayList<>(Arrays.asList(
                    "destination.csv", "destination.csf", "datas.xml"));

            for(Relationship rel : incomingRelationshipList) {
                String name = rel.getStartNode().getProperty(Fields.NAME).toString();
                assertTrue(expected.remove(name));
            }

            assertEquals(0, expected.size());

            List<Relationship> outgoingRelationshipList = getResultAsList(
                    node.getRelationships(RelTypes.PARENT, Direction.OUTGOING)
            );

            assertEquals(1, outgoingRelationshipList.size());
            assertRelation(outgoingRelationshipList.get(0), first, folder);

            tx.success();
        } catch (Exception exception) {
            exception.printStackTrace();
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testLeaf() {
        try (Transaction tx = graphDb.beginTx()) {
            Node c3poFile = graphDb.findNode(new FileLabel(), Fields.ID, c3p0);

            List<Relationship> list = getResultAsList(c3poFile.getRelationships(RelTypes.PARENT));

            assertEquals(1, list.size());
            assertRelation(list.get(0), c3p0, folder);

            List<Relationship> outgoingRelationshipList = getResultAsList(
                    c3poFile.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );

            assertEquals(0, outgoingRelationshipList.size());

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testGetParent() {
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = repository.getParent(folder);
            assertEquals(rootId, parentNode.getProperty(Fields.ID));

            Node firstNode = repository.getParent(first);
            assertEquals(folder, firstNode.getProperty(Fields.ID));

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }


    @Test(timeout = 10000)
    public void testUpdateRelationship(){
        Node nodeChild = repository.getNodeById(first);
        Node nodeParent = repository.getNodeById(rootId);

        assertTrue(repository.updateParentRelation(nodeChild, nodeParent));

        try(Transaction tx = graphDb.beginTx()) {
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(6, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            Node firstNode = graphDb.findNode(new FileLabel(), Fields.ID, first);

            assertEquals(rootId,
                    firstNode.getSingleRelationship(RelTypes.PARENT, Direction.OUTGOING)
                            .getEndNode()
                            .getProperty(Fields.ID));

            Node folderNode = graphDb.findNode(new FileLabel(), Fields.ID, folder);

            List<Relationship> relationshipList = getResultAsList(folderNode.getRelationships(RelTypes.PARENT, Direction.INCOMING));

            assertEquals(2, relationshipList.size());

            tx.success();
        } catch (Exception exception){
            fail();
        }
    }


    @Test
    public void testUpdateParentRelationFails() throws Exception {
        Node rootNode = repository.getNodeById(rootId);
        Node child = null;

        try(Transaction tx = graphDb.beginTx()){
            child = graphDb.createNode();
            tx.success();
        }catch (Exception e){

        }

        assertFalse(repository.updateParentRelation(child, rootNode));

        try(Transaction tx = graphDb.beginTx()){

            List<Relationship> incomingRelationshipList = getResultAsList(
                    rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );
            assertEquals(5, incomingRelationshipList.size());

            tx.success();

        }catch(Exception exception){
            exception.printStackTrace();
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedString(){
        assertTrue(repository.markAsDeleted(folder));

        try(Transaction tx = graphDb.beginTx()) {
            Node folderNode = graphDb.findNode(new FileLabel(), Fields.ID, folder);
            Node c3poNode = graphDb.findNode(new FileLabel(), Fields.ID, c3p0);
            Node dsStoreNode = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirVndxcFE3T0c5R1U");
            Node firstNode = graphDb.findNode(new FileLabel(), Fields.ID, first);
            Node file1 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirdEtBSVZXTzZIcUU");
            Node file2 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirU2tqQU5PTjdWd3c");
            Node file3 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirZUM4cmFQaWlScUE");

            assertDeletion(folderNode);
            assertDeletion(c3poNode);
            assertDeletion(dsStoreNode);
            assertDeletion(firstNode);
            assertDeletion(file1);
            assertDeletion(file2);
            assertDeletion(file3);

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    private void assertDeletion(Node node){
        Map<String, Object> result = node.getProperties(Fields.DELETED, Fields.PROCESSED);

        assertTrue((boolean)result.get(Fields.DELETED));
        assertFalse((boolean)result.get(Fields.PROCESSED));
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedStringFails(){
        assertFalse(repository.markAsDeleted("notExist"));
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedNode(){
        try(Transaction tx = graphDb.beginTx()) {
            Node folderNode = graphDb.findNode(new FileLabel(), Fields.ID, folder);
            Node c3poNode = graphDb.findNode(new FileLabel(), Fields.ID, c3p0);
            Node dsStoreNode = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirVndxcFE3T0c5R1U");
            Node firstNode = graphDb.findNode(new FileLabel(), Fields.ID, first);
            Node file1 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirdEtBSVZXTzZIcUU");
            Node file2 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirU2tqQU5PTjdWd3c");
            Node file3 = graphDb.findNode(new FileLabel(), Fields.ID, "0B3mMPOF_fWirZUM4cmFQaWlScUE");

            assertTrue(repository.markAsDeleted(folderNode));

            assertDeletion(folderNode);
            assertDeletion(c3poNode);
            assertDeletion(dsStoreNode);
            assertDeletion(firstNode);
            assertDeletion(file1);
            assertDeletion(file2);
            assertDeletion(file3);

            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testMarkAsDeletedNodeFails(){
        Node nodeNotExists = null;
        assertFalse(repository.markAsDeleted(nodeNotExists));

        try(Transaction tx = graphDb.beginTx()) {
            graphDb.getAllNodes().forEach(this::assertDeletionFails);
            tx.success();
        } catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testMarkAsDeletedNodeFails2(){
        Node folderNode = null;
        try(Transaction tx = graphDb.beginTx()){
            folderNode = graphDb.findNode(new FileLabel(), Fields.ID, "notExists");
            tx.success();
        } catch (Exception exception){
            fail();
        }

        assertFalse(repository.markAsDeleted(folderNode));
    }

    private void assertDeletionFails(Node node){
        assertFalse((boolean)node.getProperty(Fields.DELETED));
    }

    @Test(timeout = 100000)
    public void testGetUnProcessedFile(){
        Queue<Node> result = repository.getUnprocessedQueue();
        try(Transaction tx = graphDb.beginTx()){
            assertEquals(getResultAsList(graphDb.getAllNodes()).size(), result.size());
            tx.success();
        }catch (Exception exception){
            fail();;
        }
    }

    @Test
    public void testMarkAsProcessed() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node firstNode = repository.getNodeById(first);

            assertTrue(repository.markAsProcessed(firstNode));

            assertTrue((boolean) firstNode.getProperty(Fields.PROCESSED));

            Queue<Node> result = repository.getUnprocessedQueue();
            assertEquals(getResultAsList(graphDb.getAllNodes()).size() - 1, result.size());

            tx.success();
        }catch(Exception exception){
            fail();
        }
    }

    @Test(timeout = 100000)
    public void testMarkFileAsProcessedFails(){
        assertFalse(repository.markAsProcessed(null));
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExists(){
        File file = new File();
        file.setId("mockId");
        file.setName("newFile");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setModifiedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);
        file.setParents(Arrays.asList(new String[]{rootId}));

        assertTrue(repository.createIfNotExists(file));

        try(Transaction tx = graphDb.beginTx()){
            assertEquals(15, getResultAsList(graphDb.getAllNodes()).size());
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(6, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExistsWithoutModifiedTime(){
        File file = new File();
        file.setId("mockId");
        file.setName("newFile");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);
        file.setParents(Arrays.asList(new String[]{rootId}));

        assertTrue(repository.createIfNotExists(file));

        try(Transaction tx = graphDb.beginTx()){
            assertEquals(15, getResultAsList(graphDb.getAllNodes()).size());
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(6, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExistsParentNull(){
        File file = new File();
        file.setId("mockId");
        file.setName("newFile");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setModifiedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);
        file.setParents(new ArrayList<String>());

        assertFalse(repository.createIfNotExists(file));

        try(Transaction tx = graphDb.beginTx()){
            assertEquals(14, getResultAsList(graphDb.getAllNodes()).size());
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(5, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test(timeout = 10000)
    public void testCreateIfNotExistsFailsNodeAlreadyExists(){
        File file = new File();
        file.setId(rootId);
        file.setName("newFile");
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setModifiedTime(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);
        file.setParents(Arrays.asList(new String[]{rootId}));

        assertFalse(repository.createIfNotExists(file));

        try(Transaction tx = graphDb.beginTx()){
            assertEquals(14, getResultAsList(graphDb.getAllNodes()).size());
            Node rootNode = graphDb.findNode(new FileLabel(), Fields.ID, rootId);

            assertEquals(5, getResultAsList(rootNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)).size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testMarkAsTrashed() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node folder1 = repository.getNodeById(first);

            repository.markAsTrashed(folder1);

            assertTrue((boolean)folder1.getProperty(Fields.TRASHED));
            assertFalse((boolean) folder1.getProperty(Fields.PROCESSED));
        }catch(Exception exception){

        }
    }

    @Test
    public void testMarkAsTrashedFails() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            assertFalse(repository.markAsTrashed(null));
        }catch(Exception exception){

        }
    }

    @Test
    public void testMarkAsUnTrashed() throws Exception {
        Node folder1 = repository.getNodeById(first);
        assertTrue(repository.markAsUnTrashed(folder1));

        try(Transaction tx = graphDb.beginTx()){
            assertFalse((boolean) folder1.getProperty(Fields.TRASHED));
            assertFalse((boolean)folder1.getProperty(Fields.PROCESSED));
            tx.success();
        }catch(Exception exception){
            fail();
        }
    }

    @Test
    public void testMarkAsUnTrashedFails() throws Exception {
        assertFalse(repository.markAsTrashed(null));
    }

    @Test
    public void testUpdate() throws Exception {
        try(Transaction tx = graphDb.beginTx()){
            Node node = repository.getNodeById(first);

            File first = new File();

            first.setName("newTitle");
            first.setMimeType("application/vnd.google-apps.document");
            first.setCreatedTime(new DateTime("2016-01-07T15:14:10.751Z"));
            first.setModifiedTime(new DateTime("2016-01-07T15:14:10.751Z"));
            first.setVersion(1l);
            first.setParents(Arrays.asList(new String[]{rootId}));

            assertTrue(repository.update(node, first));

            assertEquals("newTitle", node.getProperty(Fields.NAME));
            assertEquals(new Long(1l), node.getProperty(Fields.VERSION));
            assertEquals("application/vnd.google-apps.document", node.getProperty(Fields.MIME_TYPE));

            tx.success();
        }catch(Exception exception){
            fail();
        }
    }

    @Test
    public void testUpdateFails() throws Exception {
        Node node = repository.getNodeById("folder1");
        assertFalse(repository.update(node, null));
    }

    @Test
    public void testGetUnprocessedQueue() throws Exception {
        Queue<Node> result = repository.getUnprocessedQueue();
        assertEquals(14, result.size());
    }

    @Test
    public void testGetTrashedList() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node firstNode = repository.getNodeById(first);
            repository.markAsTrashed(firstNode);

            Node folderNode = repository.getNodeById(folder);
            repository.markAsTrashed(folderNode);

            List<Node> result = repository.getTrashedList();

            assertEquals(7, result.size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }


    @Test
    public void testGetTrashedQueueEmpty() throws Exception {
        Queue<Node> result = repository.getTrashedQueue();
        assertEquals(0, result.size());
    }

    @Test
    public void testGetTrashedQueue() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node firstNode = repository.getNodeById(first);
            repository.markAsTrashed(firstNode);

            Node folderNode = repository.getNodeById(folder);
            repository.markAsTrashed(folderNode);

            assertEquals(7, repository.getTrashedQueue().size());
            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testGetTrashedQueue2() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node firstNode = repository.getNodeById(first);
            repository.markAsTrashed(firstNode);

            assertEquals(4, repository.getTrashedQueue().size());
            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testGetDeletedQueue() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node firstNode = repository.getNodeById(first);
            repository.markAsDeleted(firstNode);

            Node folderNode = repository.getNodeById(folder);
            repository.markAsDeleted(folderNode);

            assertEquals(7, repository.getDeletedQueue().size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testGetDeletedQueue2() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {
            Node firstNode = repository.getNodeById(first);
            repository.markAsDeleted(firstNode);

            assertEquals(4, repository.getDeletedQueue().size());

            tx.success();
        }catch (Exception exception) {
            fail();
        }
    }


    @Test
    public void testGetDeletedQueueEmpty() throws Exception {
        Queue<Node> result = repository.getDeletedQueue();
        assertEquals(0, result.size());
    }


    @Test
    public void testCreateParentRelation() throws Exception {
        try(Transaction tx = graphDb.beginTx()) {

            Node fileNode = graphDb.createNode();
            Node folderNode = graphDb.createNode();

            boolean result = repository.createParentRelation(fileNode, folderNode);

            assertTrue(result);

            List<Relationship> incomingRelationshipList = getResultAsList(
                    folderNode.getRelationships(RelTypes.PARENT, Direction.INCOMING)
            );

            assertEquals(1, incomingRelationshipList.size());
            tx.success();

        }catch (Exception exception) {
            fail();
        }
    }

    @Test
    public void testCreateParentRelationFails() throws Exception {
        Node rootNode = null;
        Node newNode = null;
        assertFalse(repository.createParentRelation(newNode, rootNode));
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

    private File setFile(fixtures.model.File f){
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