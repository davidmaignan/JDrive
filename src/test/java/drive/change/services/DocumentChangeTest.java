package drive.change.services;

import com.google.inject.Guice;
import drive.change.model.CustomChange;
import drive.change.modules.ChangeModule;
import drive.change.services.apply.DocumentService;
import drive.change.services.update.DocumentChangeUpdate;
import io.filesystem.modules.FileSystemModule;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-18.
 */
public class DocumentChangeTest {
    DocumentChange DocumentChange;
    DocumentService service;
    DocumentChangeUpdate update;
    CustomChange structure;

    @Before
    public void setUp() throws Exception {
        service = mock(DocumentService.class);
        update = mock(DocumentChangeUpdate.class);

        DocumentChange = new DocumentChange(service, update);
        structure = mock(CustomChange.class);
        DocumentChange.setStructure(structure);
    }

    @Test
    public void testAnnotation(){
        DocumentChange service = Guice.createInjector(
                new ChangeModule(),
                new FileSystemModule()).getInstance(DocumentChange.class);
        assertTrue(service.getService() instanceof DocumentService);
        assertTrue(service.getUpdate() instanceof DocumentChangeUpdate);
    }

    @Test
    public void executeSuccess() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = true;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertTrue(DocumentChange.execute());
    }

    @Test
    public void executeServiceFails() throws Exception {
        boolean resultService = false;

        when(service.execute()).thenReturn(resultService);

        assertFalse(DocumentChange.execute());
        verify(update, never()).execute();
    }

    @Test
    public void executeUpdateFails() throws Exception {
        boolean resultService = true;
        boolean resultUpdate = false;

        when(service.execute()).thenReturn(resultService);
        when(update.execute()).thenReturn(resultUpdate);

        assertFalse(DocumentChange.execute());
    }

}