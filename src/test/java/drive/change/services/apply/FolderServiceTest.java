package drive.change.services.apply;

import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Folder;
import org.junit.Before;
import org.junit.Test;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-19.
 */
public class FolderServiceTest {
    private CustomChange structure;
    private FileRepository fileRepository;
    private Folder folder;
    private Node newParentNode;
    private String newName;
    private FolderService service;

    @Before
    public void setUp() throws Exception {
        structure = mock(CustomChange.class);
        fileRepository = mock(FileRepository.class);
        folder = mock(Folder.class);
        newParentNode = mock(Node.class);
        newName = "newName";

        service = new FolderService(fileRepository, folder);
        service.setStructure(structure);
    }

    @Test
    public void execute() throws Exception {
        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn("test");
        when(structure.getNewName()).thenReturn(newName);
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(folder.write("test/newName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void executeFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn("test");
        when(structure.getNewName()).thenReturn(newName);
        when(structure.getNewParentNode()).thenReturn(null);
        when(folder.write("test/newName")).thenReturn(false);

        assertFalse(service.execute());
    }
}