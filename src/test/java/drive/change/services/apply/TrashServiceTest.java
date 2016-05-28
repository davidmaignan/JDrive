package drive.change.services.apply;

import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Trashed;
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
public class TrashServiceTest {

    private FileRepository fileRepository;
    private TrashService service;
    private CustomChange structure;
    private Trashed trashed;
    private Node parentNode;
    private String name;
    private String path;

    @Before
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        structure = mock(CustomChange.class);
        trashed = mock(Trashed.class);
        parentNode = mock(Node.class);
        name = "mockName";
        path = "/folder";

        service = new TrashService(fileRepository, trashed);
        service.setStructure(structure);
    }

    @Test
    public void executeWithSlash() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(trashed.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void execute() throws Exception {
        path = "folder";
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(trashed.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void executeFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(trashed.write("folder/mockName")).thenReturn(false);

        assertFalse(service.execute());
    }
}