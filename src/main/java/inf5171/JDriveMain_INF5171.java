package inf5171;

import com.google.api.services.drive.model.File;
import configuration.Configuration;
import inf5171.fixtures.FileList;
import inf5171.stats.Measure;
import inf5171.monitor.MStructureMonitor;
import inf5171.monitor.producer.FileProducer;
import inf5171.monitor.consumer.TreeConsumer;
import inf5171.utils.NodeCounter;
import io.Document;
import io.Folder;
import io.filesystem.FileSystemInterface;
import io.filesystem.FileSystemWrapperTest;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import model.types.MimeType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;

/**
 * Created by david on 2016-12-01.
 *
 * git clone -b inf5171 --single-branch https://github.com/davidmaignan/JDrive.git MAID10077306
 *
 * Build and run this main:  ./gradlew INF5171
 *
 */
public class JDriveMain_INF5171 {
//    private static FileSystemWrapperTest fs;
//    private static Configuration configuration;
    private static Map<String, List<Measure>> statisticMap;
    private static String[] methods;

    public static void main(String args[]) throws IOException, InterruptedException {

        methods = new String[]{"sequential", "prod/con", "cachedPool"};
        statisticMap = new HashMap<>();

        for (int i = 0; i < methods.length; i++) {
            statisticMap.put(methods[i], new ArrayList<>());
        }

        for (int i = 1; i < 7; i++) {
            //2 iterations for sequential avec 1 thread (moyenne)
            for (int j = 0; j < 2; j++) {
                Measure measure = new Measure();
                measure.setType(methods[0]);
                measure.setDepth(i);
                measure.setNbThreads(1);
                sequential(measure);
                statisticMap.get(methods[0]).add(measure);
            }
        }

        // i = nombre de repertoires et fichiers par niveau
        // j = nombre de threads
        for (int i = 1; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                Measure measure = new Measure();
                measure.setType(methods[1]);
                measure.setDepth(i);
                measure.setNbThreads(j*10+1);
                threadsArray(measure);
                statisticMap.get(methods[1]).add(measure);
            }
        }

        for (int i = 1; i < 7; i++) {
            for (int j = 0; j < 5; j++) {
                Measure measure = new Measure();
                measure.setType(methods[2]);
                measure.setDepth(i);
                measure.setNbThreads(j*10+1);
                cachedPool(measure);
                statisticMap.get(methods[2]).add(measure);
            }
        }

        for (int i = 0; i < methods.length; i++) {
            System.out.println(printStatistic(statisticMap.get(methods[i])));
        }
    }

    private static String printStatistic(List<Measure> list){

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%15s|", "nbThreads"));
        builder.append(String.format("%15s|", "nbFiles"));
        builder.append(String.format("%15s|", "nbNodes"));
        builder.append(String.format("%15s|", "Stage 1"));
        builder.append(String.format("%15s|", "Stage 2"));
        builder.append("\n");


        for (Measure stat: list) {
            builder.append(String.format("%15s|", String.valueOf(stat.getNbThreads())));
            builder.append(String.format("%15s|", String.valueOf(stat.getTotalFiles())));
            builder.append(String.format("%15s|", String.valueOf(stat.getTotalNodes())));
            builder.append(String.format("%15s|", String.valueOf(stat.getSeconds(0))));
            builder.append(String.format("%15s|", String.valueOf(stat.getSeconds(1))));
            builder.append("\n");
        }

        return builder.toString();
    }

    private static void sequential(Measure stats) throws IOException, InterruptedException {
        FileList fixtures = new FileList(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();

        stats.setTotalFiles(fileList.size());

        printStatus(stats);

        stats.startWatch();

        TreeBuilder treeBuilder = new TreeBuilder("root");
        MStructureMonitor<File> fileMonitor = new MStructureMonitor<>();
        FileProducer<File> fileProducer = new FileProducer<>(fileMonitor, fileList);
        fileProducer.setThreshold(300);

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();
        producerTh.join();

        treeBuilder.build(new ArrayList<>(fileMonitor.getQueue()));

        stats.stopWatch();

        stats.setTotalNodes(NodeCounter.countNodes(treeBuilder.getRoot()));

        Set<String> allItems = new HashSet<>();
        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());

        stats.setDuplicates(duplicates);
        System.out.print("Done\n");
    }

    private static void printStatus(Measure measure){
        System.out.printf("Type: %-12s - TotalFiles: %-7d - NbThreads: %-3d ",
                measure.getType(), measure.getTotalFiles(), measure.getNbThreads());
    }

    private static void threadsArray(Measure stats) throws IOException, InterruptedException {
        FileList fixtures = new FileList(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();

        stats.setTotalFiles(fileList.size());

        printStatus(stats);

        stats.startWatch();
        TreeBuilder treeBuilder = new TreeBuilder("root");
        MStructureMonitor<File> fileMonitor = new MStructureMonitor<>();
        FileProducer<File> fileProducer = new FileProducer<>(fileMonitor, fileList);
        fileProducer.setThreshold(300);

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();

        Thread[] threadsTree = new Thread[stats.getNbThreads()];

        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i] = new Thread(new TreeConsumer(fileMonitor, treeBuilder));
            threadsTree[i].start();
        }

        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i].join();
        }
        producerTh.join();

        stats.stopWatch();
        stats.setTotalNodes(NodeCounter.countNodes(treeBuilder.getRoot()));

        Set<String> allItems = new HashSet<>();
        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());

        stats.setDuplicates(duplicates);
        System.out.print("Done\n");
    }

    private static void cachedPool(Measure stats) throws IOException, InterruptedException {
        FileList fixtures = new FileList(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();

        stats.setTotalFiles(fileList.size());

        printStatus(stats);

        stats.startWatch();
        TreeBuilder treeBuilder = new TreeBuilder("root");
        MStructureMonitor<File> fileMonitor = new MStructureMonitor<>();
        FileProducer<File> fileProducer = new FileProducer<>(fileMonitor, fileList);
        fileProducer.setThreshold(300);

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();

        ExecutorService pool = Executors.newCachedThreadPool();

        for(int i = 0; i < stats.getNbThreads(); i++){
            pool.execute(new TreeConsumer(fileMonitor, treeBuilder));
        }

        producerTh.join();

        pool.shutdown();

        stats.stopWatch();

        Configuration configuration = new Configuration();
        FileSystemInterface fs = new FileSystemWrapperTest(configuration);

        //Stage 2 - Writing the files
        stats.startWatch();

//        Write Files in fs
//        WriterAction.compute(treeBuilder.getRoot(), fs);

        stats.stopWatch();

//        stats.setTotalNodes(NodeCounter.countNodes(treeBuilder.getRoot()));
//        stats.setTotalFilesWritten(FileCount.compute(fs, rootPath));
//        Set<String> allItems = new HashSet<>();
//        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
//                .filter(n -> !allItems.add(n.getId()))
//                .collect(Collectors.toSet());
//
//        stats.setDuplicates(duplicates);
        System.out.print("Done\n");
    }

//    private static void versionSequentielle() throws IOException, InterruptedException {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        configuration = new Configuration();
//        fs = new FileSystemWrapperTest(configuration);
//
//        TreeBuilder treeBuilder = new TreeBuilder("root");
//        treeBuilder.build(getFiles("fixtures/files.json"));
//
//        List<File> fileList = getFiles();
//
//        System.out.println("Total files: " + fileList.size());
//
////        treeBuilder.build(getFiles());
//
//        List<TreeNode> listNodes = treeBuilder.getNodes();
//        System.out.println(listNodes.size() + "");
//
//        MStructureMonitor<TreeNode> monitor = new MStructureMonitor<>();
//        monitor.push(treeBuilder.getRoot());
//
//        FileProducer<TreeNode> producer = new FileProducer<>(monitor, listNodes);
//
//        Thread prodThread = new Thread(producer);
//        prodThread.start();
//
//        prodThread.join();
//
//        while(! monitor.getCompleted() || monitor.size() > 0) {
//            TreeNode node = monitor.shift();
//            if(! write(node, fs)){
//                monitor.push(node);
//            }
//        }
//
//        stopWatch.stop();
//        System.out.println(stopWatch.getTime());
//    }
//
//    private static void versionProducerConsumer(int nbThreads)
//            throws IOException, InterruptedException {
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        Configuration configuration = new Configuration();
//        fs = new FileSystemWrapperTest(configuration);
//
//        TreeBuilder treeBuilder = new TreeBuilder("root");
//        treeBuilder.build(getFiles("fixtures/files.json"));
//
//        List<TreeNode> listNodes = treeBuilder.getNodes();
//        System.out.println(listNodes.size() + "");
//
//
//        MStructureMonitor<TreeNode> monitor = new MStructureMonitor<>();
//        monitor.push(treeBuilder.getRoot());
//
//        FileProducer<TreeNode> producer = new FileProducer<>(monitor, listNodes);
//
//        Thread prodThread = new Thread(producer);
//        prodThread.start();
//
//        Thread[] threads = new Thread[nbThreads];
//        Consumer[] consumers = new Consumer[nbThreads];
//
//        for (int i = 0; i < nbThreads; i++) {
//            consumers[i] = new Consumer(monitor, listNodes, fs);
//            threads[i] = new Thread(consumers[i]);
//            threads[i].start();
//        }
//
//        for (int i = 0; i < nbThreads; i++) {
//            threads[i].join();
//        }
//
//        prodThread.join();
//
//        stopWatch.stop();
//        System.out.println(stopWatch.getNanoTime() / 1.0E-9);
//
//        Path rootPath = fs.getPath(configuration.getRootFolder());
//
//        printFileSystem(rootPath);
//
//
//        int totalWrite = 0;
//        for (int i = 0; i < nbThreads; i++) {
//            totalWrite += consumers[i].getTotal().get();
//        }
//
//        System.out.println("Total write: " + totalWrite);
//    }

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
        FileList fixtures = new FileList(5);

        return fixtures.getFileList();
    }

    private static List<File> getFiles(String filename) throws IOException {
        FileList fixtures = new FileList(filename);

        return fixtures.getFileList();
    }

//    private static void version2() throws IOException {
//        FileList fixtures = new FileList("fixtures/files.json");
//        List<File> files = fixtures.getFileList();
//
//
//        TreeBuilder treeBuilder = new TreeBuilder("root");
//        treeBuilder.build(files);
//
//        TreeBuilder.printTree(treeBuilder.getRoot());
//
//
//        System.out.println(files.size() + "");
//
//
//        List<TreeNode> listNodes = treeBuilder.getNodes();
//
//        System.out.println(listNodes.size() + "");
//
//
//        Configuration configuration = new Configuration();
//        fs = new FileSystemWrapperTest(configuration);
//
//
//        ForkJoinPool pool = new ForkJoinPool( 10 );
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        Future<?>[] futures = new Future[listNodes.size()];
//
//
//
////        for (int i = 0; i < listNodes.size(); i++) {
////            futures[i] = pool.submit(
////                    ()-> write(TreeNode)
////            );
////        }
//
////        for (int i = 0; i < listNodes.size(); i++) {
////            try{
////                futures[i].get();
////            }catch (Exception e){
////
////            }
////        }
//
//        stopWatch.stop();
//
////        System.out.println(stopWatch.getNanoTime() / 1.0E-9);
//
//    }

//    private static void version1(){
//        ExecutorService pool = Executors.newFixedThreadPool(20);
//
//        int n = 10000;
//
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//
//        Thread threads[] = new Thread[n];
//        for( int i = 0; i < n; i++ ) {
//            int fi = i;
//            threads[i] = new Thread(
//                    () -> write(String.valueOf(fi)) );
//            threads[i].start();
//        }
//
//        for( int i = 0; i < n; i++ ) {
//            try { threads[i].join(); } catch( Exception e ){};
//        }
//
//        stopWatch.stop();
//
//        System.out.println(stopWatch.getNanoTime() / 1.0E-9);
//
//    }
//
//    private static boolean write(String name){
//        try {
//            sleep((long)(Math.random() * 1000));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        Folder folder = new Folder(fs);
//
//        folder.setFileId(name);
//        return folder.write(name);
//    }
}
