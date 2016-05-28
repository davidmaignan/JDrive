package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.NewFileService;
import drive.change.services.update.NewFileChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class NewFileChangeTest {
    private NewFileChange newFileChange;
    private NewFileService service;
    private NewFileChangeUpdate update;
    private CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(NewFileService.class);
        update = mock(NewFileChangeUpdate.class);

        newFileChange = new NewFileChange(service, update);
        structure = mock(CustomChange.class);
        newFileChange.setStructure(structure);
    }

    @Test
    @Ignore
    public void testAnnotation(){
        NewFileChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(NewFileChange.class);
        assertTrue(service.getService() instanceof NewFileService);
        assertTrue(service.getUpdate() instanceof NewFileChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(newFileChange.execute());

//        verify(service, times(1)).setStructure(structure);
//        verify(update, times(1)).setStructure(structure);
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(newFileChange.execute()).thenReturn(resultService);

        assertFalse(service.execute());

        verify(update, never()).execute();
//        verify(service, times(1)).setStructure(structure);
//        verify(update, times(1)).setStructure(structure);
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(newFileChange.execute());

//        verify(service, times(1)).setStructure(structure);
//        verify(update, times(1)).setStructure(structure);
    }
}