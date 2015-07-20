package org.writer;

import org.model.TreeNode;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Writer interface
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface WriterInterface {
    public boolean write(TreeNode node);
}
