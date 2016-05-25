package drive.change.model;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ChangeTreeTest {
    private ChangeRepository changeRepository;
    private FileRepository fileRepository;
    private ChangeTree changeTree;

    @Before
    public void setUp() throws IOException {
        fileRepository = mock(FileRepository.class);
        changeRepository = mock(ChangeRepository.class);

        changeTree = new ChangeTree(fileRepository, changeRepository);
    }

    @Test
    public void testExecute() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);
        Node fileNode = mock(Node.class);
        Change change = new Change();
        File file = new File();
        change.setFile(file);

        List<Node> trashedNode = mock(ArrayList.class);

        when(fileRepository.getTrashedList()).thenReturn(trashedNode);

        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(false);
        when(validChange.getFileNode()).thenReturn(fileNode);
        when(trashedNode.contains(fileNode)).thenReturn(false);
        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
        verify(fileRepository, never()).createIfNotExists(file);
    }

    @Test
    public void testExecuteNewFile() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);

        Node parentNode = mock(Node.class);
        Node newFileNode = mock(Node.class);

        Change change = new Change();
        File file = new File();
        change.setFile(file);

        List<Node> trashedNode = mock(ArrayList.class);

        when(fileRepository.getTrashedList()).thenReturn(trashedNode);

        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(true);

        //Create node
        when(fileRepository.getNodeById("parentId")).thenReturn(parentNode);
        when(fileRepository.createIfNotExists(file)).thenReturn(true);
        when(validChange.getFileNode()).thenReturn(null);
        when(trashedNode.contains(newFileNode)).thenReturn(false);
        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
    }

    @Test
    public void testExecuteTrashFile() throws Exception {
        List<ValidChange> changeList = mock(ArrayList.class);
        ValidChange validChange = mock(ValidChange.class);
        Node fileNode = mock(Node.class);

        Change change = new Change();
        File file = new File();
        change.setFile(file);

        List<Node> trashedNode = mock(ArrayList.class);
        when(fileRepository.getTrashedList()).thenReturn(trashedNode);

        when(changeList.isEmpty()).thenReturn(false, true);
        when(changeList.remove(0)).thenReturn(validChange);
        when(validChange.isNewFile()).thenReturn(false);
        when(validChange.getFileNode()).thenReturn(fileNode);
        when(trashedNode.contains(fileNode)).thenReturn(true);

        when(validChange.getChange()).thenReturn(change);

        changeTree.execute(changeList);

        verify(changeRepository, times(1)).addChange(change);
        verify(fileRepository, times(1)).markAsUnTrashed(fileNode);
    }
}