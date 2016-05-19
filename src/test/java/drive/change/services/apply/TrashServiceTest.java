package drive.change.services.apply;

import drive.change.model.CustomChange;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Created by david on 2016-05-19.
 */
public class TrashServiceTest {
    @Test
    public void execute() throws Exception {
        CustomChange structure = mock(CustomChange.class);

        TrashService service = new TrashService();
        service.setStructure(structure);

        assertTrue(service.execute());
    }
}