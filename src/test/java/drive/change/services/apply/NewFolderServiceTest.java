package drive.change.services.apply;

import com.google.api.services.drive.model.Change;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Folder;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-28.
 */
public class NewFolderServiceTest {
    private NewFolderService service;
    private FileRepository fileRepository;
    private Node fileNode;
    private Folder folderIO;
    private CustomChange structure;
    private Change change;
    private String folderID;
    private String path;
    private com.google.api.services.drive.model.File file;

    @Before
    public void setUp() throws Exception {
        structure = mock(CustomChange.class);
        fileRepository = mock(FileRepository.class);
        folderIO = mock(Folder.class);
        fileNode = mock(Node.class);
        folderID = "folderID";
        path = "mock/path/folder";

        service = new NewFolderService(fileRepository, folderIO);
        service.setStructure(structure);

        change = new Change();
        change.setFileId(folderID);
        file = new com.google.api.services.drive.model.File();
        file.setId(folderID);
        change.setFile(file);
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        when(fileRepository.createIfNotExists(file)).thenReturn(fileNode);
        when(structure.getChange()).thenReturn(change);
        when(folderIO.write(path)).thenReturn(true);
        when(fileRepository.getNodeAbsolutePath(fileNode)).thenReturn(path);

        assertTrue(service.execute());

        verify(structure, times(1)).setFileNode(fileNode);
    }

    @Test
    public void testExecuteFailNoNodeCreated() throws Exception {
        when(fileRepository.createIfNotExists(file)).thenReturn(null);
        when(structure.getChange()).thenReturn(change);
        when(folderIO.write(path)).thenReturn(true);

        assertFalse(service.execute());

        verify(structure, times(0)).setFileNode(fileNode);
        verify(fileRepository, never()).getNodeAbsolutePath(fileNode);
    }
}