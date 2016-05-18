package drive.services;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import drive.change.services.ConverterService;
import drive.change.model.ChangeTypes;
import drive.api.change.ChangeService;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-04.
 */
@RunWith(DataProviderRunner.class)
public class ConverterServiceTest {
    private ChangeRepository spyChangeRepository;
    private FileRepository spyFileRepository;
    private ChangeService spyChangeService;

    private ConverterService service;

    private static Logger logger;

    @BeforeClass
    public static void init() {
        logger = LoggerFactory.getLogger(ConverterServiceTest.class.getSimpleName());
    }

    @Before
    public void setUp() throws Exception {
        spyChangeRepository = mock(ChangeRepository.class);
        spyFileRepository = mock(FileRepository.class);
        spyChangeService = mock(ChangeService.class);

        service = new ConverterService(spyFileRepository, spyChangeRepository, spyChangeService);
    }

    @Test(timeout = 10000)
    public void testChangeNotExists() throws Exception {
        Node spyNode = mock(Node.class);

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(null);
        when(spyChangeRepository.delete(spyNode)).thenReturn(true);

        CustomChange result = service.execute(spyNode);
        assertEquals(ChangeTypes.NULL, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileNodeNotExists() throws Exception {
        Node spyNode = mock(Node.class);
        Change change = new Change();
        String fileId = "fileId";
        change.setFileId(fileId);

        when(spyChangeRepository.getId(spyNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileId)).thenReturn(null);

        CustomChange result = service.execute(spyNode);
        assertEquals(ChangeTypes.NULL, result.getType());
    }

    @Test(timeout = 10000)
    public void testDeleteChange() throws Exception {
        Node changeNode = mock(Node.class);
        Node parentNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();
        String fileId = "fileId";
        change.setFileId(fileId);
        change.setDeleted(true);


        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(spyFileRepository.getParent("fileId")).thenReturn(parentNode);
        when(spyFileRepository.getTitle(fileNode)).thenReturn("fileTitle");

        CustomChange result = service.execute(changeNode);

        assertEquals(ChangeTypes.DELETE, result.getType());
        assertEquals("fileTitle", result.getNewName());
        assertEquals("fileTitle", result.getOldName());
        assertEquals(parentNode, result.getNewParentNode());
        assertEquals(parentNode, result.getOldParentNode());
    }

    @Test(timeout = 10000)
    public void testFileRenamed(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();
        String fileId = "fileId";
        change.setFileId(fileId);
        change.setDeleted(false);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileId)).thenReturn(fileNode);


        String oldParent = "oldParent";
        String oldName = "oldName";
        String newName = "newName";

        File file = createFile(newName, oldParent);
        change.setFile(file);

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(spyFileRepository.getTitle(fileNode)).thenReturn(oldName);
        when(spyFileRepository.getNodeById(oldParent)).thenReturn(oldParentNode);


        CustomChange result = service.execute(changeNode);

        assertEquals(oldName, result.getOldName());
        assertEquals(newName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileMoved(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Node newParentNode = mock(Node.class);
        Change change = new Change();
        String fileId = "fileId";
        change.setFileId(fileId);
        change.setDeleted(false);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileId)).thenReturn(fileNode);


        String newParent = "newParent";
        String oldName = "oldName";
        String newName = "newName";

        File file = createFile(newName, newParent);
        change.setFile(file);

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(spyFileRepository.getTitle(fileNode)).thenReturn(oldName);
        when(spyFileRepository.getNodeById(newParent)).thenReturn(newParentNode);


        CustomChange result = service.execute(changeNode);

        assertEquals(oldName, result.getOldName());
        assertEquals(newName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(newParentNode, result.getNewParentNode());


        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileTrashed(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();
        String fileId = "fileId";
        change.setFileId(fileId);
        change.setDeleted(false);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileId)).thenReturn(fileNode);


        String oldParent = "oldParent";
        String oldName = "oldName";
        String newName = "newName";

        File file = createFile(newName, oldParent);

        File.Labels labels = new File.Labels();
        labels.setTrashed(true);
        file.setLabels(labels);
        change.setFile(file);

        when(spyFileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(spyFileRepository.getTitle(fileNode)).thenReturn(oldName);
        when(spyFileRepository.getNodeById(oldParent)).thenReturn(oldParentNode);


        CustomChange result = service.execute(changeNode);

        assertEquals(oldName, result.getOldName());
        assertEquals(newName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertTrue(result.getTrashed());


        assertEquals(ChangeTypes.TRASHED, result.getType());
    }

    @Test(timeout = 10000)
    public void testUpdateContent(){
        Node changeNode = mock(Node.class);
        Node fileNode = mock(Node.class);
        Node parentNode = mock(Node.class);

        String parentName = "parent";
        String fileName = "fileId";

        Change change = new Change();
        change.setFileId(fileName);
        change.setDeleted(false);

        Long changeVersion = new Long(100l);
        Long fileVersion = new Long(101l);

        when(spyChangeRepository.getId(changeNode)).thenReturn("mockNodeId");
        when(spyChangeService.get("mockNodeId")).thenReturn(change);
        when(spyFileRepository.getNodeById(fileName)).thenReturn(fileNode);


        File file = createFile(fileName, parentName);
        file.setMimeType("image/png");
        change.setFile(file);

        when(spyFileRepository.getNodeById(parentName)).thenReturn(parentNode);
        when(spyFileRepository.getParent(fileName)).thenReturn(parentNode);
        when(spyFileRepository.getTitle(fileNode)).thenReturn(fileName);

        CustomChange result = service.execute(changeNode);

        assertEquals(ChangeTypes.FILE_UPDATE, result.getType());
    }

    @DataProvider
    public static Object[][] dataProviderGetTrashed(){
        return new Object[][]{
                {null, null, null, null, false},
                {new File(), null, null, null, false},
                {new File(), false, null, null, false},
                {new File(), false, new File.Labels(), false, false},
                {new File(), true, null, false, true},
                {new File(), false, new File.Labels(), true, true},
                {new File(), true, new File.Labels(), true, true}
        };
    }

    @Test
    @UseDataProvider("dataProviderGetTrashed")
    public void testGetTrashed(File file, Boolean explicitely,
                               File.Labels labels, Boolean trashed,
                               boolean expected){
        Change change = new Change();

        if(file != null) {
            file.setExplicitlyTrashed(explicitely);
            if(labels != null){
                labels.setTrashed(trashed);
            }
            file.setLabels(labels);
        }

        change.setFile(file);

        assertEquals(expected, service.getTrashed(change));
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
}