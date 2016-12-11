package inf5171;

import com.google.api.services.drive.model.File;
import configuration.Configuration;
import inf5171.fixtures.FileFixtures;
import inf5171.monitor.Consumer;
import inf5171.monitor.MStructureMonitor;
import inf5171.monitor.Producer;
import io.Document;
import io.Folder;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import model.types.MimeType;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-01.
 */
public class JDriveMain_INF5171 {
    private static FileSystemWrapperTest fs;
    private static Configuration configuration;

    public static void main(String args[]) {
        try {
            for (int i = 0; i < 5; i++) {
                versionProducerConsumer(i * 5 + 5);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void versionProducerConsumer(int nbThreads)
            throws IOException, InterruptedException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        TreeBuilder treeBuilder = new TreeBuilder("root");
        treeBuilder.build(getFiles("fixtures/files.json"));

        List<TreeNode> listNodes = treeBuilder.getNodes();
        System.out.println(listNodes.size() + "");

        MStructureMonitor<TreeNode> monitor = new MStructureMonitor<>();
        monitor.push(treeBuilder.getRoot());

        Producer<TreeNode> producer = new Producer<>(monitor, listNodes);

        Thread prodThread = new Thread(producer);
        prodThread.start();

        Thread[] threads = new Thread[nbThreads];
        for (int i = 0; i < nbThreads; i++) {
            threads[i] = new Thread(new Consumer(monitor, listNodes, fs));
            threads[i].start();
        }

        for (int i = 0; i < nbThreads; i++) {
            threads[i].join();
        }

        prodThread.join();

        stopWatch.stop();
        System.out.println(stopWatch.getNanoTime() / 1.0E-9);

//        Path data = fs.getPath(configuration.getRootFolder());
//        Files.list(data).forEach(file -> {
//            try {
//                System.out.println(String.format("%s - %b", file, Files.isDirectory(file)));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    private static boolean write(TreeNode node, FileSystemInterface fs){
        if(node.getMimeType().equals(MimeType.FOLDER)){
            Folder folder = new Folder(fs);
            return folder.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        } else {
            Document document = new Document(fs);
            return document.write(fs.getRootPath()+ "/" + node.getAbsolutePath());
        }
    }

    private static List<File> getFiles(String filename) throws IOException {
        FileFixtures fixtures = new FileFixtures(filename);

        return fixtures.getFileList();
    }

    private static void version2() throws IOException {
        FileFixtures fixtures = new FileFixtures("fixtures/files.json");
        List<File> files = fixtures.getFileList();


        TreeBuilder treeBuilder = new TreeBuilder("root");
        treeBuilder.build(files);

        TreeBuilder.printTree(treeBuilder.getRoot());


        System.out.println(files.size() + "");


        List<TreeNode> listNodes = treeBuilder.getNodes();

        System.out.println(listNodes.size() + "");


        Configuration configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);


        ForkJoinPool pool = new ForkJoinPool( 10 );

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Future<?>[] futures = new Future[listNodes.size()];



//        for (int i = 0; i < listNodes.size(); i++) {
//            futures[i] = pool.submit(
//                    ()-> write(TreeNode)
//            );
//        }

//        for (int i = 0; i < listNodes.size(); i++) {
//            try{
//                futures[i].get();
//            }catch (Exception e){
//
//            }
//        }

        stopWatch.stop();

//        System.out.println(stopWatch.getNanoTime() / 1.0E-9);

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
            sleep((long)(Math.random() * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Folder folder = new Folder(fs);

        folder.setFileId(name);
        return folder.write(name);
    }



}
