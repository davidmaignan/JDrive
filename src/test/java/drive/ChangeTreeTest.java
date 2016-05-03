package drive;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.google.inject.Guice;
import com.google.inject.Injector;
import database.Fields;
import database.RelTypes;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import org.api.FileService;
import org.configuration.Configuration;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.neo4j.graphdb.*;
import org.neo4j.test.TestGraphDatabaseFactory;
import org.neo4j.tooling.GlobalGraphOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.writer.FileModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTreeTest {
    protected GraphDatabaseService graphDb;
    private ChangeRepository changeRepository;
    private FileRepository fileRepository;
    private FileService fileService;
    private static Logger logger;

    private ChangeTree changeTree;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger(ChangeTreeTest.class);
    }

    @Before
    public void setUp() throws IOException {
        graphDb = new TestGraphDatabaseFactory().newImpermanentDatabase();
        Configuration configuration = new Configuration();
        fileRepository = new FileRepository(graphDb, configuration);
//        changeRepository = new ChangeRepository(graphDb, configuration);

        fileService = mock(FileService.class);
        changeRepository = mock(ChangeRepository.class);

        changeTree = new ChangeTree(fileRepository, fileService, changeRepository);

        fileRepository.save(this.getRootNode());
    }

    @Test
    public void testExecuteOneChangeMoveFolder() throws Exception {

        List<Change> changeList = this.getListOneChangeMoveFolder();

        when(changeRepository.addChange(changeList.get(0))).thenReturn(true);

        changeTree.execute(changeList);

        assertNotNull(fileRepository.getNodeById("folder1"));
    }

    private List<Change> getListOneChangeMoveFolder(){

        List<Change> result = new ArrayList<>();

        File folder1 = new File();

        folder1.setTitle("folder1");
        folder1.setId("folder1");
        folder1.setMimeType("application/vnd.google-apps.folder");
        folder1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder1.setVersion(0l);

        folder1.setParents(this.getParentReferenceList(
                "folder2", true
        ));

        Change change = new Change();
        change.setId(0l);
        change.setFileId(folder1.getId());
        change.setFile(folder1);

        result.add(change);

        return result;
    }

    @Test
    public void testExecuteTwoChangeCreateFolderAndMove() throws Exception {
        List<Change> changeList = this.getListChangesCreateFolderAndMove();

        when(changeRepository.addChange(changeList.get(0))).thenReturn(true);
        when(changeRepository.addChange(changeList.get(1))).thenReturn(true);
        when(changeRepository.addChange(changeList.get(2))).thenReturn(true);
        when(changeRepository.addChange(changeList.get(3))).thenReturn(true);

        when(fileService.getFile("folder5")).thenReturn(this.generateFile("folder5", "folder6"));
        when(fileService.getFile("folder6")).thenReturn(this.generateFile("folder6", "root"));

        changeTree.execute(changeList);

        Node folder5 = fileRepository.getNodeById("folder5");
        Node folder6 = fileRepository.getNodeById("folder6");
//
        assertNotNull(folder5);
        assertNotNull(folder6);
//
        try(Transaction tx = graphDb.beginTx()) {
            assertEquals("folder5", folder5.getProperty(Fields.ID));
            assertEquals("folder6", getParentId(folder5));

            assertEquals("folder6", folder6.getProperty(Fields.ID));
            assertEquals("root", getParentId(folder6));
        }
    }

    private List<Change> getListChangesCreateFolderAndMove(){
        List<Change> result = new ArrayList<>();

        result.add(this.generateChange("folder1", "folder4"));
        result.add(this.generateChange("folder4", "folder5"));
        result.add(this.generateChange("folder5", "folder6"));
        result.add(this.generateChange("folder6", "root"));

        return result;
    }

    private Change generateChange(String fileId, String parentId) {
        File folder4 = new File();

        folder4.setTitle(fileId);
        folder4.setId(fileId);
        folder4.setMimeType("application/vnd.google-apps.folder");
        folder4.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder4.setVersion(0l);

        folder4.setParents(this.getParentReferenceList(
                parentId, true
        ));

        Change change = new Change();
        change.setId(0l);
        change.setFileId(folder4.getId());
        change.setFile(folder4);


        return change;
    }

    private File generateFile(String fileId, String parentId){
        File file = new File();

        file.setTitle(fileId);
        file.setId(fileId);
        file.setMimeType("application/vnd.google-apps.folder");
        file.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);

        file.setParents(this.getParentReferenceList(
                parentId, true
        ));

        return file;
    }

    private String getParentId(Node node) {
        return node.getSingleRelationship(
                RelTypes.PARENT, Direction.OUTGOING
        ).getEndNode().getProperty(Fields.ID).toString();
    }

    private List<Change> getListOneChangeCreateFolder(){

        List<Change> result = new ArrayList<>();

        File folder1 = new File();

        folder1.setTitle("folder4");
        folder1.setId("folder4");
        folder1.setMimeType("application/vnd.google-apps.folder");
        folder1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        folder1.setVersion(0l);

        folder1.setParents(this.getParentReferenceList(
                "root", true
        ));

        Change change = new Change();
        change.setId(0l);
        change.setFileId(folder1.getId());
        change.setFile(folder1);

        result.add(change);

        return result;
    }

    @Test
    public void testExecuteOneChangeCreateFolder() throws Exception {

        List<Change> changeList = this.getListOneChangeCreateFolder();

        when(changeRepository.addChange(changeList.get(0))).thenReturn(true);

        changeTree.execute(changeList);

        Node folder4 = fileRepository.getNodeById("folder4");

        assertNotNull(folder4);

        try(Transaction tx = graphDb.beginTx()) {
            assertEquals("folder4", folder4.getProperty(Fields.ID));

            assertEquals("root", getParentId(folder4));
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