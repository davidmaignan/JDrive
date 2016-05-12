package drive;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.ChangeInterpreted;
import drive.change.model.ChangeStruct;
import drive.change.model.ChangeTypes;
import org.api.change.ChangeService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by david on 2016-05-04.
 */
public class ChangeInterpretedTest {
    private ChangeRepository spyChangeRepository;
    private FileRepository spyFileRepository;
    private ChangeService spyChangeService;

    private ChangeInterpreted service;

    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger("DatabaseServiceTest");
    }

    @Before
    public void setUp() throws Exception {
        spyChangeRepository = mock(ChangeRepository.class);
        spyFileRepository = mock(FileRepository.class);
        spyChangeService = mock(ChangeService.class);

        service = new ChangeInterpreted(spyFileRepository, spyChangeRepository, spyChangeService);
    }

    @Test(timeout = 10000)
    public void testChangeNotExists() throws Exception {
        Node spyNode = mock(Node.class);

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(null);
        when(spyChangeRepository.delete(spyNode)).thenReturn(true);

        ChangeStruct result = service.execute(spyNode);
        assertEquals(ChangeTypes.NULL, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileNodeNotExists() throws Exception {
        Node spyNode = mock(Node.class);
        Change change = new Change();

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(spyNode)).thenReturn(null);
        when(spyChangeRepository.update(change)).thenReturn(true);

        ChangeStruct result = service.execute(spyNode);
        assertEquals(ChangeTypes.NULL, result.getType());
    }

    @Test(timeout = 10000)
    public void testSameVersionBetweenChangeAndFile() throws Exception {
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(100l);


        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        when(spyChangeRepository.update(change)).thenReturn(true);

        ChangeStruct result = service.execute(changeNode);
        assertEquals(ChangeTypes.VERSION, result.getType());
    }

    @Test(timeout = 10000)
    public void testDeleteChange() throws Exception {
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();
        change.setDeleted(true);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        ChangeStruct result = service.execute(changeNode);

        assertEquals(ChangeTypes.DELETE, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileRenamed(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node parentNode = mock(Node.class);
        Change change = new Change();
        change.setDeleted(false);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        String oldParent = "oldParent";
        String oldName = "oldName";
        String newName = "newName";

        File file = createFile(newName, oldParent);
        change.setFile(file);
        change.setFileId(file.getId());

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(parentNode);
        when(parentNode.toString()).thenReturn(oldParent);

        String oldParentPath = "/mock/oldparent";
        when(spyFileRepository.getNodeAbsolutePath(oldParent)).thenReturn(oldParentPath);

        when(spyFileRepository.getTitle(fileNode)).thenReturn(oldName);


        ChangeStruct result = service.execute(changeNode);

        assertEquals(oldParentPath, result.getOldParentPath());
        assertEquals(oldParentPath, result.getNewParentPath());
        assertEquals(oldParent, result.getOldParent());
        assertEquals(oldParent, result.getNewParent());
        assertEquals(newName, result.getNewName());
        assertEquals(oldName, result.getOldName());
        assertEquals(oldParentPath+ "/" + newName, result.getNewPath());
        assertEquals(oldParentPath+ "/" + oldName, result.getOldPath());
        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileMoved() {
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node parentNode = mock(Node.class);
        Change change = new Change();
        change.setDeleted(false);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        String newParent = "newParent";
        String oldParent = "oldParent";
        String fileName = "mockFile";

        File file = createFile(fileName, newParent);
        change.setFile(file);
        change.setFileId(file.getId());

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(parentNode);
        when(parentNode.toString()).thenReturn(oldParent);

        String oldParentPath = "/mock/oldparent";
        when(spyFileRepository.getNodeAbsolutePath(oldParent)).thenReturn(oldParentPath);

        String newParentPath = "/mock/newparent";
        when(spyFileRepository.getNodeAbsolutePath(newParent)).thenReturn(newParentPath);

        when(spyFileRepository.getTitle(fileNode)).thenReturn(fileName);

        ChangeStruct result = service.execute(changeNode);

        assertEquals(oldParentPath, result.getOldParentPath());
        assertEquals(newParentPath, result.getNewParentPath());
        assertEquals(oldParent, result.getOldParent());
        assertEquals(newParent, result.getNewParent());
        assertEquals(fileName, result.getOldName());
        assertEquals(fileName, result.getNewName());
        assertEquals(newParentPath+ "/" + fileName, result.getNewPath());
        assertEquals(oldParentPath+ "/" + fileName, result.getOldPath());
        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testUpdateContent(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node parentNode = mock(Node.class);
        Change change = new Change();
        change.setDeleted(false);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getFileNodeFromChange(changeNode)).thenReturn(fileNode);

        when(spyChangeRepository.getVersion(changeNode)).thenReturn(changeVersion);
        when(spyFileRepository.getVersion(fileNode)).thenReturn(fileVersion);

        String newParent = "oldParent";
        String oldParent = "oldParent";
        String fileName = "mockFile";

        File file = createFile(fileName, newParent);
        file.setMimeType("image/png");
        change.setFile(file);
        change.setFileId(file.getId());

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(parentNode);
        when(parentNode.toString()).thenReturn(oldParent);

        String oldParentPath = "/mock/oldparent";
        when(spyFileRepository.getNodeAbsolutePath(oldParent)).thenReturn(oldParentPath);

        when(spyFileRepository.getTitle(fileNode)).thenReturn(fileName);

        ChangeStruct result = service.execute(changeNode);

        assertEquals(oldParentPath, result.getOldParentPath());
        assertEquals(oldParentPath, result.getNewParentPath());
        assertEquals(oldParent, result.getOldParent());
        assertEquals(newParent, result.getNewParent());
        assertEquals(fileName, result.getOldName());
        assertEquals(fileName, result.getNewName());
        assertEquals(oldParentPath+ "/" + fileName, result.getNewPath());
        assertEquals(oldParentPath+ "/" + fileName, result.getOldPath());
        assertEquals(ChangeTypes.FILE_UPDATE, result.getType());
    }

    private File createFile(String title, String parent){
        File file = new File();
        file.setTitle(title);
        file.setId(title);
        file.setMimeType("application/vnd.google-apps.document");
        file.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));
        file.setVersion(0l);

        file.setParents(this.getParentReferenceList(
                parent,
                false
        ));

        file.setOwners(this.getOwnerList("David Maignan", true));

        return file;
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

    @Test(timeout = 10000)
    public void testFileNewContent(){

    }

    @Test(timeout = 10000)
    public void testFileDriveMimeType(){

    }
}