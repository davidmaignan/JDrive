package org.writer;

import com.google.api.services.drive.model.File;
import org.model.tree.TreeNode;
import org.neo4j.graphdb.Node;

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
    public abstract WriterInterface getWriter(Node node) throws UnsupportedOperationException;
}
