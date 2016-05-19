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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by david on 2016-05-19.
 */
@RunWith(DataProviderRunner.class)
public class DeleteChangeUpdateTest {

    DeleteChangeUpdate service;
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
        service = new DeleteChangeUpdate(fileRepository, changeRepository);
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
    public void testExecute(boolean changeProcessed, boolean changeDeleted, boolean fileDeleted, boolean expected) throws Exception {
        when(changeRepository.markAsProcessed(changeNode)).thenReturn(changeProcessed);
        when(changeRepository.markAsDeleted(changeNode)).thenReturn(changeDeleted);
        when(fileRepository.markAsDeleted(fileNode)).thenReturn(fileDeleted);
        when(structure.getChangeNode()).thenReturn(changeNode);
        when(structure.getFileNode()).thenReturn(fileNode);

        assertEquals(expected, service.execute());
    }
}