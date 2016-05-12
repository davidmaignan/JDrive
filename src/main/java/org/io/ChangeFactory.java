package org.io;

import com.google.api.services.drive.model.Change;
import com.google.inject.Guice;
import org.writer.FileModule;
import org.writer.file.DocumentWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-18.
 */
public class ChangeFactory {
    public static ChangeInterface get(Change change) {
        ChangeInterface service;

        if (change.getDeleted()) {
            service = Guice.createInjector(new FileModule()).getInstance(DeleteService.class);
            service.setChange(change);

            return service;
        }

        throw new UnsupportedOperationException("No change apply provided");
    }
}
