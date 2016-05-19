package drive.change.services.update;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import database.repository.ChangeRepository;
import database.repository.FileRepository;
import drive.change.model.CustomChange;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by david on 2016-05-19.
 */
@RunWith(DataProviderRunner.class)
public class MoveChangeUpdateTest {

    MoveChangeUpdate service;
    FileRepository fileRepository;
    ChangeRepository changeRepository;
    CustomChange structure;
    Node changeNode;
    Node fileNode;
    Node newParentNode;
    Change change;
    File file;

    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        changeRepository = mock(ChangeRepository.class);
        structure = mock(CustomChange.class);
        changeNode = mock(Node.class);
        fileNode = mock(Node.class);
        newParentNode = mock(Node.class);
        change = new Change();
        file = new File();
        change.setFile(file);
        service = new MoveChangeUpdate(fileRepository, changeRepository);
        service.setStructure(structure);
    }


    @DataProvider
    public static Object[][] dataProvider(){
        return new Object[][]{
                {false, false, false, false},
                {true, false, false, false},
                {false, true, false, false},
                {false, false, true, false},
                {true, true, false, false},
                {true, false, true, false},
                {false, true, true, false},
                {true, true, true, true},
        };
    }

    @Test
    @UseDataProvider("dataProvider")
    public void testExecute(boolean changeProcessed, boolean updateRelation, boolean updateFile, boolean expected)
            throws Exception {
        when(changeRepository.markAsProcessed(changeNode)).thenReturn(changeProcessed);
        when(fileRepository.updateParentRelation(fileNode, newParentNode)).thenReturn(updateRelation);
        when(fileRepository.update(fileNode, file)).thenReturn(updateFile);

        when(structure.getChangeNode()).thenReturn(changeNode);
        when(structure.getFileNode()).thenReturn(fileNode);
        when(structure.getNewParentNode()).thenReturn(newParentNode);
        when(structure.getChange()).thenReturn(change);

        assertEquals(expected, service.execute());
    }
}