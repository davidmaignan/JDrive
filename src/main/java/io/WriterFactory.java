package io;

import com.google.inject.Guice;
import io.filesystem.modules.FileSystemJmsfModule;
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

    public static WriterInterface getWriterJmsf(String mimeType) {
        if(mimeType.equals(MimeType.FOLDER)) {
            return Guice.createInjector(new FileSystemJmsfModule()).getInstance(Folder.class);
        } else if (! MimeType.all().contains(mimeType)) {
            return Guice.createInjector(new FileSystemJmsfModule()).getInstance(File.class);
        }

        return Guice.createInjector(new FileSystemJmsfModule()).getInstance(Document.class);
    }
}
