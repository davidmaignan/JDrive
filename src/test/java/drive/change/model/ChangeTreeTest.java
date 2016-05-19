package drive.change.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import database.Fields;
import database.RelTypes;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTreeTest {
    protected GraphDatabaseService graphDb;
    private ChangeRepository changeRepository;
    private FileRepository fileRepository;
    private static Logger logger;

    private ChangeTree changeTree;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger(ChangeTreeTest.class);
    }

    @Before
    public void setUp() throws IOException {
        fileRepository = mock(FileRepository.class);
        changeRepository = mock(ChangeRepository.class);

        changeTree = new ChangeTree(fileRepository, changeRepository);
    }

    @Test
    public void testExecute() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();
        change.setId(1000l);

        List<Node> trashedNode = mock(ArrayList.class);

        when(fileRepository.getTrashedList()).thenReturn(trashedNode);

        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(false);
        when(validChange.getFileNode()).thenReturn(fileNode);
        when(trashedNode.contains(fileNode)).thenReturn(false);
        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
    }


    @Test
    public void testExecuteNewFile() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);

        Node parentNode = mock(Node.class);
        Node newFileNode = mock(Node.class);

        Change change = new Change();
        File file = this.generateFile("fileId", "parentId");
        change.setFile(file);

        List<Node> trashedNode = mock(ArrayList.class);

        when(fileRepository.getTrashedList()).thenReturn(trashedNode);

        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(true);

        //Create node
        when(fileRepository.getNodeById("parentId")).thenReturn(parentNode);
        when(fileRepository.createNode(file)).thenReturn(newFileNode);
        when(fileRepository.createParentRelation(newFileNode, parentNode)).thenReturn(true);
        when(validChange.getFileNode()).thenReturn(null);
        when(trashedNode.contains(newFileNode)).thenReturn(false);
        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
    }

    @Test

    public void testExecuteTrashFile() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);
        Node fileNode = mock(Node.class);

        Change change = new Change();
        File file = this.generateFile("fileId", "parentId");
        change.setFile(file);

        List<Node> trashedNode = mock(ArrayList.class);
        when(fileRepository.getTrashedList()).thenReturn(trashedNode);


        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(false);
        when(validChange.getFileNode()).thenReturn(fileNode);
        when(trashedNode.contains(fileNode)).thenReturn(true);

        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
        verify(fileRepository, times(1)).markAsUnTrashed(fileNode);

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
}