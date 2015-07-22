package org.writer;

import org.model.tree.TreeNode;
import org.writer.WriterFactory;
import java.io.IOException;
import java.util.List;

/**
 * Tree writer service
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeWriter {
    public boolean write(TreeNode root) throws IOException {
        return writeTree(root);
    }

    /**
     * Debug code
     *
     * @param node
     */
    public boolean writeTree(TreeNode node) throws IOException, NullPointerException {
        boolean result = true;
        if (! WriterFactory.get(node).write(node)) {
            result = false;
        }

        List<TreeNode> children = node.getChildren();

        if (!children.isEmpty()) {
            for (TreeNode child : children) {
                result = writeTree(child) || result;
            }
        }

        return result;
    }
}
