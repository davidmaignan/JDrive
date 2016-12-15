package inf5171.stats;

import model.tree.TreeNode;
import org.apache.commons.lang3.time.StopWatch;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by david on 2016-12-12.
 */
public class Measure {
    private String type;
    private int depth;
    private int totalFiles;
    private int totalFilesWritten;
    private int nbThreads;
    private long[] elapsedTime;
    private int stage;
    private int totalNodes;
    private StopWatch stopWatch;
    private Set<TreeNode> duplicates;

    public Measure(){
        stopWatch = new StopWatch();
        duplicates = new HashSet<>();
        elapsedTime = new long[4];
        stage = 0;
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

    public int getStage(){
        return stage;
    }

    public void setElapsedTime(int stage, long time){
        elapsedTime[stage] = time;
    }

    public void stopWatch(){
        stopWatch.stop();

        elapsedTime[stage] = stopWatch.getNanoTime();
        stage++;
    }

    public int getTotalFilesWritten() {
        return totalFilesWritten;
    }

    public void setTotalFilesWritten(int totalFileWritten) {
        this.totalFilesWritten = totalFileWritten;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Long getElapsedTime(int stage) {
        return elapsedTime[stage];
    }

    public int getTotalNodes() {
        return totalNodes;
    }

    public void setTotalNodes(int totalNodes) {
        this.totalNodes = totalNodes;
    }

    public double getSeconds(int stage){
        return (double) elapsedTime[stage] / 1000000000.0;

    }
}
