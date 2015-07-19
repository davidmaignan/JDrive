package org.writer;

import org.model.TreeNode;
import org.model.types.MimeType;

/**
 * WriterFactory
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class WriterFactory {

    /**
     * Get writer by mime type
     *
     * @param node
     * @return WriterInterface
     * @throws Exception
     */
    public static WriterInterface get(TreeNode node) throws Exception{

        if(node.getMimeType().equals(MimeType.FOLDER)) {
            return new FolderWriter(node);
        } else if (node.getMimeType().equals(MimeType.DOCUMENT)) {
            return new DocumentWriter(node);
        } else {
            throw new Exception("Unknown mime type writer" + node.getMimeType());
        }
    }
}
