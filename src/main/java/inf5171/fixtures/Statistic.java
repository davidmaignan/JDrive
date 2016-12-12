package inf5171.fixtures;

import model.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 2016-12-12.
 */
public class Statistic {
    private int depth;
    private int totalFiles;
    private int nbThreads;
    private long elapsedTime;
    private int totalNodes;
    private StopWatch stopWatch;
    private Set<TreeNode> duplicates;

    public Statistic(){
        stopWatch = new StopWatch();
        duplicates = new HashSet<>();
    }

    public Set<TreeNode> getDuplicates() {
        return duplicates;
    }

    public void setDuplicates(Set<TreeNode> duplicates) {
        this.duplicates = duplicates;
    }

    public void startWatch(){
        stopWatch.reset();

        stopWatch.start();
    }

    public void stopWatch(){
        stopWatch.stop();

        elapsedTime = stopWatch.getNanoTime();
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public int getNbThreads() {
        return nbThreads;
    }

    public void setNbThreads(int nbThreads) {
        this.nbThreads = nbThreads;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public double getSeconds(){
        return (double) elapsedTime / 1000000000.0;

    }
}
