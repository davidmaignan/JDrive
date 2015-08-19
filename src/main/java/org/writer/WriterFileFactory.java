package org.writer;

import com.google.api.services.drive.model.File;
import com.google.inject.Guice;
import com.tinkerpop.blueprints.Vertex;
import org.db.Fields;
import org.model.tree.TreeNode;
import org.model.types.MimeType;
import org.writer.file.DocumentWriter;
import org.writer.file.FileWriter;

/**
 * Writer file factory
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class WriterFileFactory extends AbstractFactory {
    @Override
    public WriterInterface getWriter(TreeNode node) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot use a file factory to write a treenode");
    }

    @Override
    public WriterInterface getWriter(Vertex vertex) throws UnsupportedOperationException{
        WriterInterface writer;
        String mimeType = vertex.getProperty(Fields.MIME_TYPE);

        if (mimeType.equals(MimeType.FOLDER)) {
            throw new UnsupportedOperationException();
        } else if (mimeType.equals(MimeType.DOCUMENT)) {
            writer = Guice.createInjector(new FileModule()).getInstance(DocumentWriter.class);
            ((DocumentWriter)writer).setVertex(vertex);
        } else {
            writer = Guice.createInjector(new FileModule()).getInstance(FileWriter.class);
            ((FileWriter)writer).setVertex(vertex);

        }

        return writer;
    }
}
