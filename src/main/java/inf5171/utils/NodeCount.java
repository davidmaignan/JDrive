package inf5171.utils;

import model.tree.TreeNode;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Created by david on 2016-12-12.
 */
public class NodeCount extends RecursiveTask<Integer> {

    private TreeNode node;
    private int total;

    public NodeCount(TreeNode node){
        this.node = node;
    }

    @Override
    protected Integer compute() {

        total = 1;

        if(node.getChildren().size() > 0){
            for(TreeNode child : node.getChildren()){
                NodeCount childNodeCount =  new NodeCount(child);
                total += childNodeCount.compute();
            }
        }

        return total;
    }

    public static Integer countNodes(TreeNode root){
        ForkJoinPool pool = new ForkJoinPool();

        Integer result = pool.invoke(new NodeCount(root));

        pool.shutdown();

        return result;
    }
}
