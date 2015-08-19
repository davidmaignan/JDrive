package org.writer;

import org.model.tree.TreeNode;
import java.io.IOException;
import java.util.List;

/**
 * Tree writer service
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeWriter {
    public boolean write(TreeNode root) throws IOException, Exception {
        return writeTree(root);
    }

    /**
     * Debug code
     *
     * @param node
     */
    public boolean writeTree(TreeNode node) throws IOException, NullPointerException, Exception {
        boolean result = true;
        if (! FactoryProducer.getFactory("NODE").getWriter(node).write()) {
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
