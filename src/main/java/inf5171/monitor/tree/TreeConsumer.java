package inf5171.monitor.tree;

import com.google.api.services.drive.model.File;
import inf5171.monitor.MStructureMonitor;
import model.tree.TreeBuilder;
import model.tree.TreeNode;

/**
 * Created by david on 2016-12-12.
 */
public class TreeConsumer implements Runnable{

    private final MStructureMonitor<File> fileMonitor;
    private final TreeBuilder treeBuilder;

    public TreeConsumer(MStructureMonitor<File> fileMonitor, TreeBuilder treeBuilder){
        this.fileMonitor = fileMonitor;
        this.treeBuilder = treeBuilder;
    }

    @Override
    public void run() {
        while(uncompleted() || completedNotEmpty()) {
            File file = fileMonitor.shift();

            if (file != null){
//                System.out.println(file.getId());

                TreeNode node = null;

                if (file != null){
                    while(node == null){
//                        System.out.println(file.getId());
                        node = treeBuilder.insertFile(file);
                        if(node == null) {
                            try {
                                Thread.sleep(50L);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

//                    Version 2: on remet le fichier dans la queue
//                    TreeNode node = treeBuilder.insertFile(file);
//
//                    if(node == null){
//                        System.out.printf("File: %s - Node: \n", file.getId());
//                        fileMonitor.push(file);
//                    }

//                System.out.printf("File: %s - Node: %s\n", file.getId(), node.getName());
                } else {
//                    System.out.println("File null");
                }

//                TreeNode node = treeBuilder.insertFile(file);
//
//                if(node == null){
//                    System.out.printf("File: %s - Node: \n", file.getId());
//                    fileMonitor.push(file);
//                }

//                System.out.printf("File: %s - Node: %s\n", file.getId(), node.getName());
            } else {
//                System.out.println("File null");
            }
        }
    }

    private boolean completedNotEmpty(){
        return fileMonitor.getCompleted() && fileMonitor.size() > 0;
    }

    private  boolean uncompleted(){
        return ! fileMonitor.getCompleted();
    }
}
