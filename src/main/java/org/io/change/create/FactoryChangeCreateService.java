package org.io.change.create;

import com.google.api.services.drive.model.Change;
import com.google.inject.Guice;
import org.io.ChangeInterface;
import model.types.MimeType;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class FactoryChangeCreateService {

    public ChangeInterface get(Change change){
        String mimeType = change.getFile().getMimeType();

        if (mimeType.equals(MimeType.FOLDER)) {
            return Guice.createInjector(new WriterChangeCreateModule()).getInstance(CreateFolderService.class);
        } else if (mimeType.equals(MimeType.DOCUMENT)) {
            return Guice.createInjector(new WriterChangeCreateModule()).getInstance(CreateDocumentService.class);
        } else {
            return Guice.createInjector(new WriterChangeCreateModule()).getInstance(CreateFileService.class);
        }
    }
}
