package org.writer;

import com.google.api.services.drive.model.File;
import com.tinkerpop.blueprints.Vertex;
import org.model.tree.TreeNode;

/**
 * Abstract writer factory
 *
 * Google drive api can be requested for:
 *  - list of files: which will be wrapped with a treeNode
 *  - list of changes: which will be wrapped with a vertex from the database
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public abstract class AbstractFactory {
    public abstract WriterInterface getWriter(TreeNode node) throws UnsupportedOperationException;
    public abstract WriterInterface getWriter(Vertex vertex) throws UnsupportedOperationException;
}
