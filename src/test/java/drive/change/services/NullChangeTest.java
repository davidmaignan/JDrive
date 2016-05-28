package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.NullService;
import drive.change.services.update.NullChangeUpdate;
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
public class NullChangeTest {
    NullChange NullChange;
    NullService service;
    NullChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(NullService.class);
        update = mock(NullChangeUpdate.class);

        NullChange = new NullChange(service, update);
        structure = mock(CustomChange.class);
        NullChange.setStructure(structure);
    }

    @Test
    @Ignore
    public void testAnnotation(){
        NullChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(NullChange.class);
        assertTrue(service.getService() instanceof NullService);
        assertTrue(service.getUpdate() instanceof NullChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(NullChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(NullChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(NullChange.execute());
    }

}