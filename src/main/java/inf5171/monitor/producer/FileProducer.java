package inf5171.monitor.producer;

import com.google.api.services.drive.model.File;
import inf5171.monitor.MStructure;

import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class FileProducer<T> implements Runnable {
    private List<T> fileList;
    private MStructure<T> monitor;

    private int threshold = 300; // Number of files returned per request

    public FileProducer(MStructure<T> monitor){
        this.monitor = monitor;
    }

    public void setFileList(List<T> fileList){
        this.fileList = fileList;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public void run() {
        T[] list = (T[]) fileList.toArray();
        int total = list.length;
        int index = 0;

        while(index < total){
            try {
                sleep(100L);    // estimation assez optimiste du temps de réponse de l'api
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int last = (index < total - threshold)? index + threshold : total;

            monitor.push(Arrays.asList(Arrays.copyOfRange(list, index, last)));
            index += threshold;
        }

        monitor.setCompleted(true);
    }
}
