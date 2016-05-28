package drive.change.services;

import drive.change.model.ChangeTypes;
import drive.change.model.CustomChange;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by david on 2016-05-18.
 */
public class ChangeFactoryServiceTest {
    @Test
    @Ignore
    public void get() {
        CustomChange structure = mock(CustomChange.class);

        when(structure.getType()).thenReturn(
                ChangeTypes.DELETE,
                ChangeTypes.MOVE,
                ChangeTypes.FILE_UPDATE,
                ChangeTypes.FOLDER_UPDATE,
                ChangeTypes.DOCUMENT,
                ChangeTypes.TRASHED,
                ChangeTypes.NULL,
                ChangeTypes.VERSION,
                ChangeTypes.NEW_FILE,
                ChangeTypes.NEW_FOLDER,
                ChangeTypes.UNTRASHED
                );

        assertTrue(ChangeFactoryService.get(structure) instanceof DeleteChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof MoveChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof FileChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof FolderChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof DocumentChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof TrashChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof NullChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof NullChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof NewFileChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof NewFolderChange);
        assertTrue(ChangeFactoryService.get(structure) instanceof UntrashChange);
    }
}