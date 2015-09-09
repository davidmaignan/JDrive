package org.writer;

import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.Node;

/**
 * Abstract writer factory
 *
 * Need to return a Writer for different types of situation;
 *  - file
 *  - node
 *  - change
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public abstract class AbstractFactory {
    public abstract WriterInterface getWriter(TreeNode node) throws UnsupportedOperationException;
    public abstract WriterInterface getWriter(Node node) throws UnsupportedOperationException;
    public abstract WriterInterface getWriter(Change change) throws UnsupportedOperationException;
}
