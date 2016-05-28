package drive.change.services.apply;

import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.Delete;
import org.junit.Test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-19.
 */
public class DeleteServiceTest {

    private FileRepository fileRepository;
    private DeleteService service;
    private CustomChange structure;
    private Delete delete;
    private Node parentNode;
    private String name;
    private String path;

    @Before
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        structure = mock(CustomChange.class);
        delete = mock(Delete.class);
        parentNode = mock(Node.class);
        name = "mockName";
        path = "/folder";

        service = new DeleteService(fileRepository, delete);
        service.setStructure(structure);
    }

    @Test
    public void executeWithSlash() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(delete.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void execute() throws Exception {
        path = "folder";
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(delete.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void executeFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(delete.write("folder/mockName")).thenReturn(false);

        assertFalse(service.execute());
    }
}