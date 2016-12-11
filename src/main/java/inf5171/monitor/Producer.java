package inf5171.monitor;

import model.tree.TreeNode;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class Producer<T> implements Runnable {

    private List<TreeNode> listNodes;
    private MStructure<T> monitor;

    public Producer(MStructure<T> monitor, List<TreeNode> list){
        this.monitor = monitor;
        this.listNodes = list;
    }

    @Override
    public void run() {
        while( ! listNodes.isEmpty()){
            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(listNodes.size() > 100){
                List<TreeNode> subList = listNodes.subList(0, 100);
                if(monitor.push((List<T>) subList)){
                    listNodes.removeAll(subList);
                }

            } else {
                if(monitor.push((List<T>) listNodes)){
                    listNodes.clear();
                }
            }
        }

        monitor.setCompleted(true);
    }
}
