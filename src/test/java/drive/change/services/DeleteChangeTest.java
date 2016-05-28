package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.DeleteService;
import drive.change.services.update.DeleteChangeUpdate;
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
public class DeleteChangeTest {
    DeleteChange deleteChange;
    DeleteService service;
    DeleteChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(DeleteService.class);
        update = mock(DeleteChangeUpdate.class);

        deleteChange = new DeleteChange(service, update);
        structure = mock(CustomChange.class);
        deleteChange.setStructure(structure);
    }

    @Test
    @Ignore
    public void testAnnotation(){
        DeleteChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(DeleteChange.class);
        assertTrue(service.getService() instanceof DeleteService);
        assertTrue(service.getUpdate() instanceof DeleteChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(deleteChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(deleteChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(deleteChange.execute());
    }

}