package inf5171.monitor;

import model.tree.TreeNode;

import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class Producer<T> implements Runnable {

    private List<T> listNodes;
    private MStructure<T> monitor;
    private static System output;

    private int threshold = 300;

    public Producer(MStructure<T> monitor, List<T> list){
        this.monitor = monitor;
        this.listNodes = list;
    }

    public void setOutput(System output){
        this.output = output;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void run() {
        output.out.print("Producer progression: -");
        T[] list = (T[]) listNodes.toArray();
        int total = list.length;
        int index = 0;

        while(index < total){
            try {
                sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int last = (index < total - threshold)? index + threshold : total;

            monitor.push(Arrays.asList(Arrays.copyOfRange(list, index, last)));
            index += threshold;
            System.out.print("-");
        }

        output.out.print(" producer completed!");

        monitor.setCompleted(true);
    }
}
