package inf5171.monitor.consumer;

import inf5171.monitor.MStructure;
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
        while(uncompleted() || completedNotEmpty()) {
            System.out.println(uncompleted() +":"+ completedNotEmpty()+ "");
            TreeNode node = monitor.shift();
            if( ! write(node, fs)){
                monitor.push(node);
//                try {
//                    sleep(100L);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            } else {
                total.getAndIncrement();
            }
        }
    }

    private boolean completedNotEmpty(){
        return monitor.getCompleted() && monitor.size() > 0;
    }

    private  boolean uncompleted(){
        return ! monitor.getCompleted();
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
