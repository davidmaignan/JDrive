package inf5171.monitor.actions;

import io.Document;
import io.Folder;
import io.filesystem.FileSystemInterface;
import model.tree.TreeNode;
import model.types.MimeType;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Created by david on 2016-12-14.
 */
public class WriteTreeAction extends RecursiveAction {

    private TreeNode node;
    public static FileSystemInterface fs;

    public WriteTreeAction(TreeNode node){
        this.node = node;
    }


    @Override
    protected void compute() {
        write(node);
        if(node.getChildren().size() > 0){
            for (TreeNode child : node.getChildren()) {
                new WriteTreeAction(child).compute();
            }
        }
    }

    private boolean write(TreeNode node){
        if(node.getMimeType().equals(MimeType.FOLDER)){
            Folder folder = new Folder(fs);
            return folder.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        } else {
            Document document = new Document(fs);
            return document.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        }
    }

    public static void compute(TreeNode root, FileSystemInterface fs){
        WriteTreeAction.fs = fs;

        ForkJoinPool pool = new ForkJoinPool();

        pool.invoke(new WriteTreeAction(root));

        pool.shutdown();

    }


}
