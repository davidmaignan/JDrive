package io;

import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import io.filesystem.modules.FileSystemModule;
import model.types.MimeType;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-01-12.
 */
public class WriterFactory {

    public static WriterInterface getWriter(String mimeType) {
        if(mimeType.equals(MimeType.FOLDER)) {
            return Guice.createInjector(new FileSystemModule()).getInstance(Folder.class);
        } else if (! MimeType.all().contains(mimeType)) {
            return Guice.createInjector(new FileSystemModule()).getInstance(File.class);
        }

        return Guice.createInjector(new FileSystemModule()).getInstance(Document.class);
    }
}
