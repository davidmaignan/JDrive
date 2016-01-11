package org.writer;

import com.google.api.services.drive.model.Change;
import model.tree.TreeNode;
import model.types.MimeType;
import com.google.inject.Guice;
import org.neo4j.graphdb.Node;
import org.writer.node.DocumentWriter;
import org.writer.node.FileWriter;
import org.writer.node.FolderWriter;

/**
 * JDrive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class WriterNodeFactory extends AbstractFactory {
    @Override
    public WriterInterface getWriter(TreeNode node) {

        if(node.getMimeType().equals(MimeType.FOLDER)) {
            return new FolderWriter(node);
        } else if (MimeType.all().contains(node.getMimeType())) {
            return new DocumentWriter(node);
        } else {
            FileWriter writer = Guice.createInjector(new FileModule()).getInstance(FileWriter.class);
            writer.setNode(node);

            return writer;
        }
    }

    @Override
    public org.writer.file.FolderWriter getWriter(Node node) {
        throw new UnsupportedOperationException("You cannot use a node factory to write a db node");
    }

    @Override
    public WriterInterface getWriter(Change change) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("You cannot use a node factory to write a change");
    }
}
