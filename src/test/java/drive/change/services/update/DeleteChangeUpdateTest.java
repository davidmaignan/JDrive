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
    CustomChange structure;
    Node fileNode;


    @Before
    public void setUp() throws Exception {
        fileRepository = mock(FileRepository.class);
        structure = mock(CustomChange.class);
        fileNode = mock(Node.class);
        service = new DeleteChangeUpdate(fileRepository);
        service.setStructure(structure);
    }

    @DataProvider
    public static Object[][] dataProvider(){
        return new Object[][]{
                {false, false},
                {true, true},
        };
    }

    @Test
    @UseDataProvider("dataProvider")
    public void testExecute(boolean fileDeleted, boolean expected) throws Exception {
        when(fileRepository.markAsDeleted(fileNode)).thenReturn(fileDeleted);
        when(structure.getFileNode()).thenReturn(fileNode);

        assertEquals(expected, service.execute());
    }
}