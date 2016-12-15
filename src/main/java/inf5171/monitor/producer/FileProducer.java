package inf5171.monitor.producer;

import inf5171.monitor.MStructure;

import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class FileProducer<T> implements Runnable {
    private List<T> listNodes;
    private MStructure<T> monitor;

    private int threshold = 300; // Number of files returned per request

    public FileProducer(MStructure<T> monitor, List<T> list){
        this.monitor = monitor;
        this.listNodes = list;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void run() {
        System.out.print("FileProducer progression: -");
        T[] list = (T[]) listNodes.toArray();
        int total = list.length;
        int index = 0;

        while(index < total){
//            System.out.println("BBB");
            try {
                sleep(100L);    // To simulate request to api and response.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int last = (index < total - threshold)? index + threshold : total;

            monitor.push(Arrays.asList(Arrays.copyOfRange(list, index, last)));
            index += threshold;
            System.out.print("-");
        }

        System.out.print(" producer completed!");

        monitor.setCompleted(true);
    }
}
