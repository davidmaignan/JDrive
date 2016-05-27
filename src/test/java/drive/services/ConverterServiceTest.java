package drive.services;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
//import com.google.api.services.drive.model.ParentReference;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import database.labels.FileLabel;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import drive.change.services.ConverterService;
import drive.change.model.ChangeTypes;
import model.types.MimeType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-04.
 */
@RunWith(DataProviderRunner.class)
public class ConverterServiceTest {
    private FileRepository fileRepository;

    private ConverterService service;

    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);

        service = new ConverterService(fileRepository);
    }

    @Test(timeout = 10000)
    public void testNewFolder() throws Exception {
        String fileId = "fileId";
        String parentId = "parentId";

        Node spyNode = mock(Node.class);
        Change change = new Change();
        change.setFileId(fileId);
        File file = new File();
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FOLDER);
        change.setFile(file);

        when(fileRepository.getNodeById(fileId)).thenReturn(null);
        when(fileRepository.getNodeById(parentId)).thenReturn(null);

        CustomChange result = service.execute(change);
        assertEquals(ChangeTypes.NEW_FOLDER, result.getType());
    }

    @Test(timeout = 10000)
    public void testNewFile() throws Exception {
        String fileId = "fileId";
        String parentId = "parentId";

        Node spyNode = mock(Node.class);
        Change change = new Change();
        change.setFileId(fileId);
        File file = new File();
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FILE);
        change.setFile(file);

        when(fileRepository.getNodeById(fileId)).thenReturn(null);
        when(fileRepository.getNodeById(parentId)).thenReturn(null);

        CustomChange result = service.execute(change);
        assertEquals(ChangeTypes.NEW_FILE, result.getType());
    }

    @Test(timeout = 10000)
    public void testDeleteChange() throws Exception {
        String fileId = "fileId";

        Node parentNode = mock(Node.class);
        Node fileNode = mock(Node.class);

        Change change = new Change();
        change.setFileId(fileId);
        change.setRemoved(true);

        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(fileId)).thenReturn(parentNode);
        when(fileRepository.getName(fileNode)).thenReturn("fileTitle");

        CustomChange result = service.execute(change);

        assertEquals(ChangeTypes.DELETE, result.getType());
        assertEquals("fileTitle", result.getNewName());
        assertEquals("fileTitle", result.getOldName());
        assertEquals(parentNode, result.getNewParentNode());
        assertEquals(parentNode, result.getOldParentNode());
        assertTrue(result.getDeleted());
        assertEquals(change, result.getChange());
        assertEquals(fileNode, result.getFileNode());
    }

    @Test(timeout = 10000)
    public void testFileTrashed(){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldParent = "oldParent";
        String oldName = "oldName";
        String newName = "newName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Node newParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(newName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FILE);
        file.setTrashed(true);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(newParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(newName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(newParentNode, result.getNewParentNode());
        assertTrue(result.getTrashed());


        assertEquals(ChangeTypes.TRASHED, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileRenamed(){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldParent = "oldParent";
        String oldName = "oldName";
        String newName = "newName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(newName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FILE);
        file.setTrashed(false);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(newName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertFalse(result.getTrashed());


        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testFileMoved(){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldParent = "oldParent";
        String oldName = "oldName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Node newParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(oldName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FILE);
        file.setTrashed(false);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(newParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(oldName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(newParentNode, result.getNewParentNode());
        assertFalse(result.getTrashed());


        assertEquals(ChangeTypes.MOVE, result.getType());
    }

    @Test(timeout = 10000)
    public void testUpdateContent(){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldName = "oldName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(oldName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType("notGoogleMimeType");
        file.setTrashed(false);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(oldName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertFalse(result.getTrashed());


        assertEquals(ChangeTypes.FILE_UPDATE, result.getType());
    }

    @Test(timeout = 10000)
    public void testUpdateFolderMimeType(){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldName = "oldName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(oldName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(MimeType.FOLDER);
        file.setTrashed(false);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(oldName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertFalse(result.getTrashed());


        assertEquals(ChangeTypes.FOLDER_UPDATE, result.getType());
    }

    @DataProvider
    public static Object[][] dataProviderMimeTypes(){
        return new Object[][]{
                {MimeType.AUDIO, true},
                {MimeType.DOCUMENT, true},
                {MimeType.DRAWING, true},
                {MimeType.FILE, true},
                {MimeType.FOLDER, false},
                {MimeType.FORM, true},
                {MimeType.FUSIONTABLE, true},
                {MimeType.PHOTO, true},
                {MimeType.PRESENTATION, true},
                {MimeType.SCRIPTS, true},
                {MimeType.SITES, true},
                {MimeType.SPREADSHIT, true},
                {MimeType.UNKNOW, true},
                {MimeType.VIDEO, true},

        };
    }

    @Test(timeout = 10000)
    @UseDataProvider("dataProviderMimeTypes")
    public void testUpdateDocumentMimeType(String mimeType, boolean expected){
        String fileId = "fileId";
        String parentId = "parentId";

        String oldName = "oldName";

        Node fileNode = mock(Node.class);
        Node oldParentNode = mock(Node.class);
        Change change = new Change();

        change.setFileId(fileId);
        change.setRemoved(false);

        File file = new File();
        file.setName(oldName);
        file.setId(fileId);
        file.setParents(Arrays.asList(new String[]{parentId}));
        file.setMimeType(mimeType);
        file.setTrashed(false);
        change.setFile(file);


        when(fileRepository.getNodeById(fileId)).thenReturn(fileNode);
        when(fileRepository.getParent(change.getFileId())).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);
        when(fileRepository.getNodeById(parentId)).thenReturn(oldParentNode);
        when(fileRepository.getName(fileNode)).thenReturn(oldName);


        CustomChange result = service.execute(change);

        assertEquals(oldName, result.getOldName());
        assertEquals(oldName, result.getNewName());
        assertEquals(oldParentNode, result.getOldParentNode());
        assertEquals(oldParentNode, result.getNewParentNode());
        assertFalse(result.getTrashed());


        assertEquals(expected, result.getType() == ChangeTypes.DOCUMENT);
    }

    @DataProvider
    public static Object[][] dataProviderGetTrashed(){
        return new Object[][]{
                {null, null, null, false},
                {new File(), null, null, false},
                {new File(), false, null, false},
                {new File(), false, false, false},
                {new File(), true, null, true},
                {new File(), false, true, true},
                {new File(), true, true, true}
        };
    }

    @Test
    @UseDataProvider("dataProviderGetTrashed")
    public void testGetTrashed(File file, Boolean explicitly,
                               Boolean trashed, boolean expected){
        Change change = new Change();

        if(file != null) {
            file.setExplicitlyTrashed(explicitly);
            file.setTrashed(trashed);
        }

        change.setFile(file);

        assertEquals(expected, service.getTrashed(change));
    }
}