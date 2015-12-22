package org.writer;

import com.google.api.services.drive.model.Change;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.Node;

/**
 * Writer file factory
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class WriterFileFactory extends AbstractFactory {
    @Override
    public WriterInterface getWriter(TreeNode node) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot use a file factory to write a tree node");
    }

    @Override
    public WriterInterface getWriter(Node node) throws UnsupportedOperationException{
        WriterInterface writer = null;
//        String mimeType = vertex.getProperty(Fields.MIME_TYPE);
//
//        if (mimeType.equals(MimeType.FOLDER)) {
//            throw new UnsupportedOperationException();
//        } else if (mimeType.equals(MimeType.DOCUMENT)) {
//            writer = Guice.createInjector(new FileModule()).getInstance(DocumentWriter.class);
////            ((DocumentWriter)writer).setVertex(vertex);
//        } else {
//            writer = Guice.createInjector(new FileModule()).getInstance(FileWriter.class);
////            ((FileWriter)writer).setVertex(vertex);
//
//        }

        return writer;
    }

    @Override
    public WriterInterface getWriter(Change change) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot use a file factory to write a change");
    }
}
