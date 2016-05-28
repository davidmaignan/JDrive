package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.TrashService;
import drive.change.services.apply.UntrashService;
import drive.change.services.update.TrashChangeUpdate;
import drive.change.services.update.UntrashChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class UntrashChangeTest {
    UntrashChange untrashChange;
    UntrashService service;
    UntrashChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(UntrashService.class);
        update = mock(UntrashChangeUpdate.class);

        untrashChange = new UntrashChange(service, update);
        structure = mock(CustomChange.class);
        untrashChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        UntrashChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(UntrashChange.class);
        assertTrue(service.getService() instanceof UntrashService);
        assertTrue(service.getUpdate() instanceof UntrashChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(untrashChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(untrashChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(untrashChange.execute());
    }
}