package inf5171.monitor;

import io.Document;
import io.File;
import io.Folder;
import io.filesystem.FileSystemInterface;
import model.tree.TreeNode;
import model.types.MimeType;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class Consumer implements Runnable {
    private List<TreeNode> listNodes;
    private MStructure<TreeNode> monitor;
    private FileSystemInterface fs;
    private AtomicInteger total = new AtomicInteger(0);

    public Consumer(MStructure<TreeNode> monitor, List<TreeNode> list, FileSystemInterface fs){
        this.monitor = monitor;
        this.listNodes = list;
        this.fs = fs;
    }

    @Override
    public void run() {
        while(! monitor.getCompleted() || monitor.size() > 0) {
            TreeNode node = monitor.shift();
            if(! write(node, fs)){
                monitor.push(node);
            } else {
                total.getAndIncrement();
            }

            try {
                sleep(10L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public AtomicInteger getTotal(){
        return total;
    }

    private static boolean write(TreeNode node, FileSystemInterface fs){
        if(node.getMimeType().equals(MimeType.FOLDER)){
            Folder folder = new Folder(fs);
            return folder.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        } else {
            Document document = new Document(fs);
            return document.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        }
    }

}
