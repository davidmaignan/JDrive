package inf5171;

import com.google.api.services.drive.model.File;
import configuration.Configuration;
import inf5171.fixtures.FileFixtures;
import inf5171.fixtures.Statistic;
import inf5171.fixtures.Sum;
import inf5171.monitor.Consumer;
import inf5171.monitor.MStructureMonitor;
import inf5171.monitor.Producer;
import inf5171.monitor.tree.TreeConsumer;
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
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-01.
 */
public class JDriveMain_INF5171 {
    private static FileSystemWrapperTest fs;
    private static Configuration configuration;
    private static Map<String, List<Statistic>> statisticMap;
    private static String[] methods;

    public static void main(String args[]) throws IOException, InterruptedException {

        methods = new String[]{"sequential", "prod/con"};
        statisticMap = new HashMap<>();

        statisticMap.put(methods[0], new ArrayList<>());
        statisticMap.put(methods[1], new ArrayList<>());

//        try {
//            for (int i = 0; i < 3; i++) {
//                versionSequentielle();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//        try {
//            for (int i = 0; i < 5; i++) {
//                versionProducerConsumer(5);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//
        for (int i = 1; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                Statistic statistic = new Statistic();
                statistic.setDepth(i);
                statistic.setNbThreads(j*7+1);
                test(statistic);
                statisticMap.get(methods[0]).add(statistic);
            }
        }

        for (int i = 1; i < 6; i++) {
            for (int j = 0; j < 5; j++) {
                Statistic statistic = new Statistic();
                statistic.setDepth(i);
                statistic.setNbThreads(1);
                test2(statistic);
                statisticMap.get(methods[1]).add(statistic);
            }
        }

        System.out.println(printStatistic(statisticMap.get(methods[0])));
        System.out.println(printStatistic(statisticMap.get(methods[1])));
    }

    private static String printStatistic(List<Statistic> list){

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%15s|", "nbThreads"));
        builder.append(String.format("%15s|", "nbFiles"));
        builder.append(String.format("%15s|", "nbNodes"));
        builder.append(String.format("%15s|", "Time"));
        builder.append("\n");


        for (Statistic stat: list) {
            builder.append(String.format("%15s|", String.valueOf(stat.getNbThreads())));
            builder.append(String.format("%15s|", String.valueOf(stat.getTotalFiles())));
            builder.append(String.format("%15s|", String.valueOf(stat.getTotalNodes())));
            builder.append(String.format("%15s|", String.valueOf(stat.getSeconds())));
            builder.append("\n");
        }

        return builder.toString();
    }

    private static void test2(Statistic stats) throws IOException, InterruptedException {
        FileFixtures fixtures = new FileFixtures(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();

        stats.setTotalFiles(fileList.size());
        stats.startWatch();

        TreeBuilder treeBuilder = new TreeBuilder("root");

        MStructureMonitor<File> fileMonitor = new MStructureMonitor<>();
        Producer<File> fileProducer = new Producer<>(fileMonitor, fileList);
        fileProducer.setThreshold(300);

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();
        producerTh.join();

        treeBuilder.build(new ArrayList<>(fileMonitor.getQueue()));

        stats.stopWatch();

        stats.setTotalNodes(Sum.countNodes(treeBuilder.getRoot()));

        Set<String> allItems = new HashSet<>();
        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());

        stats.setDuplicates(duplicates);
        System.out.print("Completed\n");
    }


    private static void test(Statistic stats) throws IOException, InterruptedException {
        FileFixtures fixtures = new FileFixtures(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();

        stats.setTotalFiles(fileList.size());

        stats.startWatch();

        TreeBuilder treeBuilder = new TreeBuilder("root");

        MStructureMonitor<File> fileMonitor = new MStructureMonitor<>();
        Producer<File> fileProducer = new Producer<>(fileMonitor, fileList);
        fileProducer.setThreshold(300);

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();

        TreeConsumer treeConsumer = new TreeConsumer(fileMonitor, treeBuilder);
        Thread[] threadsTree = new Thread[stats.getNbThreads()];

        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i] = new Thread(treeConsumer);
            threadsTree[i].start();
        }

        producerTh.join();
        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i].join();
        }

        stats.stopWatch();
        stats.setTotalNodes(Sum.countNodes(treeBuilder.getRoot()));

        Set<String> allItems = new HashSet<>();
        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());

        stats.setDuplicates(duplicates);
        System.out.print("Completed\n");
    }

    private static void versionSequentielle() throws IOException, InterruptedException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        configuration = new Configuration();
        fs = new FileSystemWrapperTest(configuration);

        TreeBuilder treeBuilder = new TreeBuilder("root");
        treeBuilder.build(getFiles("fixtures/files.json"));

        List<File> fileList = getFiles();

        System.out.println("Total files: " + fileList.size());

//        treeBuilder.build(getFiles());

        List<TreeNode> listNodes = treeBuilder.getNodes();
        System.out.println(listNodes.size() + "");

        MStructureMonitor<TreeNode> monitor = new MStructureMonitor<>();
        monitor.push(treeBuilder.getRoot());

        Producer<TreeNode> producer = new Producer<>(monitor, listNodes);

        Thread prodThread = new Thread(producer);
        prodThread.start();

        prodThread.join();

        while(! monitor.getCompleted() || monitor.size() > 0) {
            TreeNode node = monitor.shift();
            if(! write(node, fs)){
                monitor.push(node);
            }
        }

        stopWatch.stop();
        System.out.println(stopWatch.getTime());
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
        Consumer[] consumers = new Consumer[nbThreads];

        for (int i = 0; i < nbThreads; i++) {
            consumers[i] = new Consumer(monitor, listNodes, fs);
            threads[i] = new Thread(consumers[i]);
            threads[i].start();
        }

        for (int i = 0; i < nbThreads; i++) {
            threads[i].join();
        }

        prodThread.join();

        stopWatch.stop();
        System.out.println(stopWatch.getNanoTime() / 1.0E-9);

        Path rootPath = fs.getPath(configuration.getRootFolder());

        printFileSystem(rootPath);


        int totalWrite = 0;
        for (int i = 0; i < nbThreads; i++) {
            totalWrite += consumers[i].getTotal().get();
        }

        System.out.println("Total write: " + totalWrite);
    }

    private static void printFileSystem(Path path) throws IOException {
        Files.list(path).forEach(file -> {
            try {
                System.out.println(String.format("%s - %b", file, Files.isDirectory(file)));

                if(Files.isDirectory(file)){
                   printFileSystem(file);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    private static List<File> getFiles() throws IOException {
        FileFixtures fixtures = new FileFixtures(5);

        return fixtures.getFileList();
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
