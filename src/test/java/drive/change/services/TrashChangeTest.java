package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.TrashService;
import drive.change.services.update.TrashChangeUpdate;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class TrashChangeTest {
    private static Logger logger = LoggerFactory.getLogger(TrashChange.class.getSimpleName());

    TrashChange TrashChange;
    TrashService service;
    TrashChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(TrashService.class);
        update = mock(TrashChangeUpdate.class);

        TrashChange = new TrashChange(service, update);
        structure = mock(CustomChange.class);
        TrashChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        TrashChange service = Guice.createInjector(new ChangeModule()).getInstance(TrashChange.class);
        assertTrue(service.getService() instanceof TrashService);
        assertTrue(service.getUpdate() instanceof TrashChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(TrashChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(TrashChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(TrashChange.execute());
    }

}