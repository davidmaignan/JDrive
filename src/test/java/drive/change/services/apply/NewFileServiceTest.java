package drive.change.services.apply;

import com.google.api.services.drive.model.Change;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.File;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-28.
 */
public class NewFileServiceTest {
    private NewFileService service;
    private FileRepository fileRepository;
    private Node fileNode;
    private File fileIO;
    private CustomChange structure;
    private Change change;
    private String fileId;
    private String path;
    private com.google.api.services.drive.model.File file;

    @Before
    public void setUp() throws Exception {
        structure = mock(CustomChange.class);
        fileRepository = mock(FileRepository.class);
        fileIO = mock(File.class);
        fileNode = mock(Node.class);
        fileId = "fileId";
        path = "mock/path/file";

        service = new NewFileService(fileRepository, fileIO);
        service.setStructure(structure);

        change = new Change();
        change.setFileId(fileId);
        file = new com.google.api.services.drive.model.File();
        file.setId(fileId);
        change.setFile(file);
    }

    @Test
    public void testExecuteSuccess() throws Exception {
        when(fileRepository.createIfNotExists(file)).thenReturn(fileNode);
        when(structure.getChange()).thenReturn(change);
        when(fileIO.write(path)).thenReturn(true);
        when(fileRepository.getNodeAbsolutePath(fileNode)).thenReturn(path);

        assertTrue(service.execute());

        verify(structure, times(1)).setFileNode(fileNode);
        verify(fileIO, times(1)).setFileId(fileId);
    }

    @Test
    public void testExecuteFailNoNodeCreated() throws Exception {
        when(fileRepository.createIfNotExists(file)).thenReturn(null);
        when(structure.getChange()).thenReturn(change);
        when(fileIO.write(path)).thenReturn(true);

        assertFalse(service.execute());

        verify(structure, times(0)).setFileNode(fileNode);
        verify(fileIO, times(0)).setFileId(fileId);
        verify(fileRepository, never()).getNodeAbsolutePath(fileNode);
    }

}