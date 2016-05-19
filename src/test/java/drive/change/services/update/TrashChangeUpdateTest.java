package drive.change.services.update;

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
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


/**
 * Created by david on 2016-05-19.
 */
@RunWith(DataProviderRunner.class)
public class TrashChangeUpdateTest {

    TrashChangeUpdate service;
    FileRepository fileRepository;
    ChangeRepository changeRepository;
    CustomChange structure;
    Node changeNode;
    Node fileNode;


    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        changeRepository = mock(ChangeRepository.class);
        structure = mock(CustomChange.class);
        changeNode = mock(Node.class);
        fileNode = mock(Node.class);
        service = new TrashChangeUpdate(fileRepository, changeRepository);
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
    public void testExecute(boolean changeProcessed, boolean changeTrashed, boolean fileTrashed, boolean expected) throws Exception {
        when(changeRepository.markAsProcessed(changeNode)).thenReturn(changeProcessed);
        when(changeRepository.markAsTrashed(changeNode)).thenReturn(changeTrashed);
        when(fileRepository.markAsTrashed(fileNode)).thenReturn(fileTrashed);
        when(structure.getChangeNode()).thenReturn(changeNode);
        when(structure.getFileNode()).thenReturn(fileNode);

        assertEquals(expected, service.execute());
    }
}