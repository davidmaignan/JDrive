package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.FileService;
import drive.change.services.update.FileChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class FileChangeTest {
    FileChange FileChange;
    FileService service;
    FileChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(FileService.class);
        update = mock(FileChangeUpdate.class);

        FileChange = new FileChange(service, update);
        structure = mock(CustomChange.class);
        FileChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        FileChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(FileChange.class);
        assertTrue(service.getService() instanceof FileService);
        assertTrue(service.getUpdate() instanceof FileChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(FileChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(FileChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(FileChange.execute());
    }

}