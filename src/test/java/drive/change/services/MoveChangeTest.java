package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.MoveService;
import drive.change.services.update.MoveChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class MoveChangeTest {
    private static Logger logger = LoggerFactory.getLogger(MoveChange.class.getSimpleName());

    MoveChange moveChange;
    MoveService service;
    MoveChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(MoveService.class);
        update = mock(MoveChangeUpdate.class);

        moveChange = new MoveChange(service, update);
        structure = mock(CustomChange.class);
        moveChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        MoveChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(MoveChange.class);
        assertTrue(service.getService() instanceof MoveService);
        assertTrue(service.getUpdate() instanceof MoveChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(moveChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(moveChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(moveChange.execute());
    }

}