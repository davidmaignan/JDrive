package drive.change.services.update;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


/**
 * Created by david on 2016-05-19.
 */
@RunWith(DataProviderRunner.class)
public class UnTrashChangeUpdateTest {

    private UntrashChangeUpdate service;
    private FileRepository fileRepository;
    private CustomChange structure;
    private Node fileNode;
    private Node oldParentNode;
    private Node newParentNode;
    private Change change;
    private File file;

    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        structure = mock(CustomChange.class);
        fileNode = mock(Node.class);

        oldParentNode = mock(Node.class);
        newParentNode = mock(Node.class);

        change = new Change();
        file = new File();
        change.setFile(file);

        service = new UntrashChangeUpdate(fileRepository);
        service.setStructure(structure);
    }

    @DataProvider
    public static Object[][] dataProviderExecute(){
        return new Object[][]{
                {false, false},
                {true, true},
        };
    }


    @Test
    @UseDataProvider("dataProviderExecute")
    public void testExecute(boolean update, boolean expected){
        when(structure.getOldParentNode()).thenReturn(oldParentNode);
        when(structure.getNewParentNode()).thenReturn(oldParentNode);
        when(structure.getFileNode()).thenReturn(fileNode);
        when(structure.getChange()).thenReturn(change);

        when(fileRepository.update(fileNode, file)).thenReturn(update);

        assertEquals(expected, service.execute());

        verify(fileRepository, never()).updateParentRelation(fileNode, newParentNode);
    }

    @DataProvider
    public static Object[][] dataProviderDifferentParent(){
        return new Object[][]{
                {false, false, false},
                {true, false, false},
                {false, true, false},
                {true, true, true},
        };
    }

    @Test
    @UseDataProvider("dataProviderDifferentParent")
    public void testExecuteDifferentParent(boolean updateParentRelation, boolean update, boolean expected){
        when(structure.getOldParentNode()).thenReturn(oldParentNode);
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getFileNode()).thenReturn(fileNode);
        when(structure.getChange()).thenReturn(change);

        when(fileRepository.updateParentRelation(fileNode, newParentNode)).thenReturn(updateParentRelation);
        when(fileRepository.update(fileNode, file)).thenReturn(update);

        assertEquals(expected, service.execute());
    }
}