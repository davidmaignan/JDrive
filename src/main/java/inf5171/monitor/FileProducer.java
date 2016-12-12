package inf5171.monitor;

import com.google.api.services.drive.model.File;
import model.tree.TreeNode;

import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-11.
 */
public class FileProducer<T> implements Runnable {

    private int threshold = 10;

    private List<File> fileList;
    private MStructure<T> monitor;

    public FileProducer(MStructure<T> monitor, List<File> list){
        this.monitor = monitor;
        this.fileList = list;
    }

    @Override
    public void run() {
        System.out.println("Producing files ");
        while( ! fileList.isEmpty()){

            try {
                sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(fileList.size() > 300){
                List<File> subList = fileList.subList(0, 300);
                if(monitor.push((List<T>) subList)){
                    fileList.removeAll(subList);
                }

            } else {
                if(monitor.push((List<T>) fileList)){
                    fileList.clear();
                }
            }
        }

        monitor.setCompleted(true);
    }
}
