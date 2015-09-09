package org.writer;

import com.google.api.services.drive.model.Change;
import com.google.inject.Guice;
import org.db.DatabaseModule;
import org.model.tree.TreeNode;
import org.model.types.MimeType;
import org.neo4j.graphdb.Node;
import org.io.change.writer.FolderWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-08-26.
 */
public class WriterChangeFactory extends AbstractFactory {
    @Override
    public WriterInterface getWriter(TreeNode node) {
        throw new UnsupportedOperationException("You cannot use a node factory to write a vertex");
    }

    @Override
    public org.writer.file.FolderWriter getWriter(Node node) {
        throw new UnsupportedOperationException("You cannot use a node factory to write a vertex");
    }

    @Override
    public WriterInterface getWriter(Change change) throws UnsupportedOperationException {
        WriterInterface writer = null;
////        String mimeType = change.getFile().getMimeType();
//        String mimeType = MimeType.FOLDER;
////
//        if (mimeType.equals(MimeType.FOLDER)) {
//            writer = Guice.createInjector(new DatabaseModule()).getInstance(FolderWriter.class);
//            ((FolderWriter)writer).setChange(change);
//
//        } else if (mimeType.equals(MimeType.DOCUMENT)) {
//            //writer = Guice.createInjector(new DatabaseModule()).getInstance(DocumentWriter.class);
//            //((DocumentWriter)writer).setChange(change);
//
//        } else {
//            throw new UnsupportedOperationException("You cannot use a node factory to write a vertex");
////            writer = Guice.createInjector(new FileModule()).getInstance(FileWriter.class);
////            ((FileWriter)writer).setChange(change);
//
//        }

        return writer;
    }
}