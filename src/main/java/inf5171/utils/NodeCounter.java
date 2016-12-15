package inf5171.utils;

import model.tree.TreeNode;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Created by david on 2016-12-12.
 */
public class NodeCounter extends RecursiveTask<Integer> {

    private TreeNode node;
    private int total;

    public NodeCounter(TreeNode node){
        this.node = node;
    }

    @Override
    protected Integer compute() {

        total = 1;

        if(node.getChildren().size() > 0){
            for(TreeNode child : node.getChildren()){
                NodeCounter childNodeCounter =  new NodeCounter(child);
                total += childNodeCounter.compute();
            }
        }

        return total;
    }

    public static Integer countNodes(TreeNode root){
        ForkJoinPool pool = new ForkJoinPool();

        Integer result = pool.invoke(new NodeCounter(root));

        pool.shutdown();

        return result;
    }
}
