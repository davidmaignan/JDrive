package io;

import com.google.inject.Guice;
import com.google.inject.Injector;
import database.DatabaseModule;
import model.types.MimeType;

/**
 * Created by david on 2016-01-12.
 */
public class WriterFactory {

    public static WriterInterface getWriter(String mimeType) throws Exception {
        if(mimeType.equals(MimeType.FOLDER)) {
            return new Folder();
        } else if (! MimeType.all().contains(mimeType)) {
            return Guice.createInjector(new DatabaseModule()).getInstance(File.class);
        }

        return Guice.createInjector(new DatabaseModule()).getInstance(Document.class);
    }
}
