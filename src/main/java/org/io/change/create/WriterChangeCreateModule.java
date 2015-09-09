package org.io.change.create;

import com.google.inject.AbstractModule;
import org.io.annotation.*;
import org.io.change.writer.DocumentWriter;
import org.io.change.writer.FileWriter;
import org.io.change.writer.FolderWriter;
import org.io.change.writer.WriterChangeInterface;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class WriterChangeCreateModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WriterChangeInterface.class).annotatedWith(File.class).to(FileWriter.class);
        bind(WriterChangeInterface.class).annotatedWith(Folder.class).to(FolderWriter.class);
        bind(WriterChangeInterface.class).annotatedWith(Document.class).to(DocumentWriter.class);
    }
}
