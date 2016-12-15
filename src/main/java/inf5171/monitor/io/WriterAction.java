package inf5171.monitor.io;

import io.Document;
import io.Folder;
import io.WriterInterface;
import io.filesystem.FileSystemInterface;
import model.tree.TreeNode;
import model.types.MimeType;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by david on 2016-12-14.
 */
public class WriterAction extends RecursiveAction {

    private TreeNode node;
    public static FileSystemInterface fs;

    public WriterAction(TreeNode node){
        this.node = node;
    }


    @Override
    protected void compute() {
        write(node);
        if(node.getChildren().size() > 0){
            for (TreeNode child : node.getChildren()) {
                new WriterAction(child).compute();
            }
        }
    }

    private boolean write(TreeNode node){
        WriterInterface writer;
        String path = fs.getRootPath() + "/" + node.getAbsolutePath();

        if(node.getMimeType().equals(MimeType.FOLDER)){
            writer = new Folder(fs);
        } else {
            writer = new Document(fs);
        }

        boolean result = false;

        while( ! result){
            result = writer.write(path);
        }

        return result;
    }

    public static void compute(TreeNode root, FileSystemInterface fs){
        WriterAction.fs = fs;
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(new WriterAction(root));
        pool.shutdown();
    }
}
