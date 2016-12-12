package inf5171.fixtures;

import model.tree.TreeNode;
import org.neo4j.cypher.internal.frontend.v2_3.ast.In;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Created by david on 2016-12-12.
 */
public class Sum extends RecursiveTask<Integer> {

    private TreeNode node;
    private int total;

    public Sum(TreeNode node){
        this.node = node;
    }

    @Override
    protected Integer compute() {

        total = 1;

        if(node.getChildren().size() > 0){
            for(TreeNode child : node.getChildren()){
                Sum childSum =  new Sum(child);
                total += childSum.compute();
            }
        }

        return total;
    }

    public static Integer countNodes(TreeNode root){
        return ForkJoinPool.commonPool().invoke(new Sum(root));
    }
}
