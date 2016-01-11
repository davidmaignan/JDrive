package org.writer;

import model.tree.TreeNode;

import java.util.List;

/**
 * Tree writer service
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeWriter {
    public boolean write(TreeNode root) throws Exception {
        return writeTree(root);
    }

    /**
     * Write a tree
     *
     * @param node TreeNode
     */
    public boolean writeTree(TreeNode node) throws Exception {
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
