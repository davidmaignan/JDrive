package drive.change.services.apply;

import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Move;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by david on 2016-05-19.
 */
public class MoveServiceTest {
    private MoveService service;
    private FileRepository fileRepository;
    private Move move;
    private CustomChange structure;
    private Node newParentNode;
    private Node oldParentNode;
    private String newName;
    private String oldName;

    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        move = mock(Move.class);
        structure = mock(CustomChange.class);
        newParentNode = mock(Node.class);
        oldParentNode = mock(Node.class);
        newName = "newName";
        oldName = "oldName";

        service = new MoveService(fileRepository, move);
        service.setStructure(structure);
    }

    @Test
    public void testExecute() throws Exception {
        when(fileRepository.getNodeAbsolutePath(oldParentNode)).thenReturn("old");
        when(structure.getOldParentNode()).thenReturn(oldParentNode);
        when(structure.getOldName()).thenReturn(oldName);

        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn("new");
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getNewName()).thenReturn(newName);

        when(move.write("old/oldName", "new/newName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void testExecuteFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(oldParentNode)).thenReturn("old");
        when(structure.getOldParentNode()).thenReturn(oldParentNode);
        when(structure.getOldName()).thenReturn(oldName);

        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn("old");
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getNewName()).thenReturn(newName);

        when(move.write("old/oldName", "new/newName")).thenReturn(false);

        assertFalse(service.execute());
    }

}