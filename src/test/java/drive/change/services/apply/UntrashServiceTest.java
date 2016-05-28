package drive.change.services.apply;

import database.repository.FileRepository;
import drive.change.model.CustomChange;
import io.File;
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
public class UntrashServiceTest {

    private FileRepository fileRepository;
    private UntrashService service;
    private CustomChange structure;
    private File file;
    private Node parentNode;
    private String name;
    private String path;

    @Before
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        structure = mock(CustomChange.class);
        file = mock(File.class);
        parentNode = mock(Node.class);
        name = "mockName";
        path = "/folder";

        service = new UntrashService(fileRepository, file);
        service.setStructure(structure);
    }

    @Test
    public void executeWithSlash() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(file.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void execute() throws Exception {
        path = "folder";
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(file.write("folder/mockName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void executeFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(parentNode)).thenReturn(path);
        when(structure.getOldParentNode()).thenReturn(parentNode);
        when(structure.getOldName()).thenReturn(name);

        when(file.write("folder/mockName")).thenReturn(false);

        assertFalse(service.execute());
    }
}