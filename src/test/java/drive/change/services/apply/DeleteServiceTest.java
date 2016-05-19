package drive.change.services.apply;

import drive.change.model.CustomChange;
import org.junit.Test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by david on 2016-05-19.
 */
public class DeleteServiceTest {
    @Test
    public void execute() throws Exception {
        CustomChange structure = mock(CustomChange.class);

        DeleteService service = new DeleteService();
        service.setStructure(structure);

        assertTrue(service.execute());
    }
}