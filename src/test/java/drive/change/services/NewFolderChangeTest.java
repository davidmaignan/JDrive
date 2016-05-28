package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.FileService;
import drive.change.services.apply.NewFileService;
import drive.change.services.apply.NewFolderService;
import drive.change.services.update.FileChangeUpdate;
import drive.change.services.update.NewFolderChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class NewFolderChangeTest {
    private NewFolderChange newFolderChange;
    private NewFolderService service;
    private NewFolderChangeUpdate update;
    private CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(NewFolderService.class);
        update = mock(NewFolderChangeUpdate.class);

        newFolderChange = new NewFolderChange(service, update);
        structure = mock(CustomChange.class);
        newFolderChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        NewFolderChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(NewFolderChange.class);
        assertTrue(service.getService() instanceof NewFolderService);
        assertTrue(service.getUpdate() instanceof NewFolderChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(newFolderChange.execute());

//        verify(service, times(1)).setStructure(structure);
//        verify(update, times(1)).setStructure(structure);
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(newFolderChange.execute()).thenReturn(resultService);

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

        assertFalse(newFolderChange.execute());

//        verify(service, times(1)).setStructure(structure);
//        verify(update, times(1)).setStructure(structure);
    }
}