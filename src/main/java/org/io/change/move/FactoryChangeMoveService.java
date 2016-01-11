package org.io.change.move;

import com.google.api.services.drive.model.Change;
import com.google.inject.Guice;
import org.io.ChangeInterface;
import model.types.MimeType;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class FactoryChangeMoveService {

    public ChangeInterface get(Change change){
        String mimeType = change.getFile().getMimeType();

        if (mimeType.equals(MimeType.FOLDER)) {
            return Guice.createInjector(new WriterChangeMoveModule()).getInstance(MoveFolderService.class);
        } else if (mimeType.equals(MimeType.DOCUMENT)) {
            return Guice.createInjector(new WriterChangeMoveModule()).getInstance(MoveDocumentService.class);
        } else {
            return Guice.createInjector(new WriterChangeMoveModule()).getInstance(MoveFileService.class);
        }
    }
}
