package inf5171;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.common.jimfs.Jimfs;
import com.google.gson.GsonBuilder;
import configuration.Configuration;
import inf5171.deserializer.DateTimeDeserializer;
import io.Folder;
import io.filesystem.FileSystemWrapperTest;
import model.tree.TreeBuilder;
import org.apache.commons.lang3.time.StopWatch;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by david on 2016-12-01.
 */
public class JDriveMain_INF5171 {

    private static FileSystemWrapperTest fs;
    private static Configuration configuration;

    public static void main(String args[]) throws IOException {

        List<File> list = new ArrayList<>();



        TreeBuilder treeBuilder = new TreeBuilder("root");
        treeBuilder.build(list);

        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);


        version1();

//        Path data = fs.getPath(configuration.getRootFolder());
//
//        Files.list(data).forEach(file -> {
//            try {
//                System.out.println(String.format("%s", file));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    private static void version1(){
        ExecutorService pool = Executors.newFixedThreadPool(20);

        int n = 10000;

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Thread threads[] = new Thread[n];
        for( int i = 0; i < n; i++ ) {
            int fi = i;
            threads[i] = new Thread(
                    () -> write(String.valueOf(fi)) );
            threads[i].start();
        }

        for( int i = 0; i < n; i++ ) {
            try { threads[i].join(); } catch( Exception e ){};
        }

        stopWatch.stop();

        System.out.println(stopWatch.getNanoTime() / 1.0E-9);

    }

    private static boolean write(String name){
        try {
            Thread.sleep((long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Folder folder = new Folder(fs);

        folder.setFileId(name);
        return folder.write(name);
    }



}
