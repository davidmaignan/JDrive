package org.io.change.move;

import com.google.inject.AbstractModule;
import org.io.annotation.Document;
import org.io.annotation.File;
import org.io.annotation.Folder;
import org.io.change.writer.*;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-27.
 */
public class WriterChangeMoveModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(WriterChangeInterface.class).annotatedWith(File.class).to(FileWriter.class);
        bind(WriterChangeInterface.class).annotatedWith(Folder.class).to(MoveFolderWriter.class);
        bind(WriterChangeInterface.class).annotatedWith(Document.class).to(MoveDocumentWriter.class);
    }
}
