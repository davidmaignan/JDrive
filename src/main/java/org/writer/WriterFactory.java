package org.writer;

import com.google.inject.Guice;
import org.model.tree.TreeNode;
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
    public static WriterInterface get(TreeNode node){
        System.out.println(node.getTitle() + " : " + node.getMimeType());
        if(node.getMimeType().equals(MimeType.FOLDER)) {
            return new FolderWriter();
        } else if (MimeType.all().contains(node.getMimeType())) {
            return new DocumentWriter();
        } else {
            return Guice.createInjector(new FileModule()).getInstance(FileWriter.class);
        }
    }
}
