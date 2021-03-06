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
public class FileServiceTest {
    private FileService service;
    private FileRepository fileRepository;
    private File file;
    private CustomChange structure;
    private Node newParentNode;
    private String newName;

    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        file = mock(File.class);
        structure = mock(CustomChange.class);
        newParentNode = mock(Node.class);
        newName = "newName";

        service = new FileService(fileRepository, file);
        service.setStructure(structure);
    }

    @Test
    public void execute() throws Exception {
        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn("test");
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getNewName()).thenReturn(newName);
        when(file.write("test/newName")).thenReturn(true);

        assertTrue(service.execute());
    }

    @Test
    public void executeFails() throws Exception {
        when(fileRepository.getNodeAbsolutePath(newParentNode)).thenReturn(null);
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getNewName()).thenReturn(newName);
        when(file.write("test/newName")).thenReturn(false);

        assertFalse(service.execute());
    }

}