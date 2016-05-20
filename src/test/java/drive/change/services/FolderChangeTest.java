package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.FolderService;
import drive.change.services.update.FolderChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class FolderChangeTest {
    FolderChange folderChange;
    FolderService service;
    FolderChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(FolderService.class);
        update = mock(FolderChangeUpdate.class);

        folderChange = new FolderChange(service, update);
        structure = mock(CustomChange.class);
        folderChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        FolderChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()
        ).getInstance(FolderChange.class);
        assertTrue(service.getService() instanceof FolderService);
        assertTrue(service.getUpdate() instanceof FolderChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(folderChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(folderChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(folderChange.execute());
    }

}