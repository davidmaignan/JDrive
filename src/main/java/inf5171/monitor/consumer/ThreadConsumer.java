package inf5171.monitor.consumer;

import com.google.api.services.drive.model.File;
import inf5171.monitor.MStructureMonitor;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import org.neo4j.cypher.internal.frontend.v2_3.ast.In;

import java.util.concurrent.Callable;

/**
 * Created by david on 2016-12-12.
 */
public class ThreadConsumer implements Runnable{

    private final MStructureMonitor<File> fileMonitor;
    private final TreeBuilder treeBuilder;

    public ThreadConsumer(MStructureMonitor<File> fileMonitor, TreeBuilder treeBuilder){
        this.fileMonitor = fileMonitor;
        this.treeBuilder = treeBuilder;
    }

    private boolean completedNotEmpty(){
        return fileMonitor.getCompleted() && fileMonitor.size() > 0;
    }

    private  boolean uncompleted(){
        return ! fileMonitor.getCompleted();
    }

    @Override
    public void run() {
        while(uncompleted() || completedNotEmpty()) {
            File file = fileMonitor.shift();

            TreeNode node = null;

            if (file != null){
                while(node == null){
                    node = treeBuilder.insertFile(file);
                    if(node == null) {
                        try {
                            Thread.sleep(50L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
