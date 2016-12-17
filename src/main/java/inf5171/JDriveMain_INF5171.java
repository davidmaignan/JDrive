package inf5171;

import com.google.api.services.drive.model.File;
import configuration.Configuration;
import inf5171.fixtures.FileList;
import inf5171.stats.Measure;
import inf5171.monitor.MStructureMonitor;
import inf5171.monitor.producer.FileProducer;
import inf5171.monitor.consumer.TreeConsumer;
import inf5171.stats.Report;
import inf5171.utils.NodeCount;
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
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    private static int fileIndex = 4;
    private static int threadsIndex = 7;
    private static int thresholdIndex = 1000;

    private static TreeBuilder treeBuilder;
    private static MStructureMonitor<File> fileMonitor;
    private static FileProducer<File> fileProducer;

    public static void main(String args[]) throws IOException, InterruptedException {
        methods = new String[]{"sequential", "threads", "forkPool"};
        statisticMap = new HashMap<>();

        for (int i = 0; i < methods.length; i++) {
            statisticMap.put(methods[i], new ArrayList<>());
        }

        //Version sequentielle
        for (int i = 1; i < fileIndex; i++) {
            //2 iterations for sequentialMethod avec 1 thread (pour calculer une moyenne)
            for (int j = 0; j < 2; j++) {
                Measure measure = new Measure();
                measure.setType(methods[0]);
                measure.setDepth(i);
                measure.setNbThreads(1);
                sequentialMethod(measure);
                statisticMap.get(methods[0]).add(measure);
            }
        }

        //Version tableau de threads
        for (int i = 1; i < fileIndex; i++) {
            for (int j = 0; j < threadsIndex; j++) {
                Measure measure = new Measure();
                measure.setType(methods[1]);
                measure.setDepth(i);
                measure.setNbThreads(j * 30 + 1);
                threadsArrayMethod(measure);
                statisticMap.get(methods[1]).add(measure);
            }
        }

        //Version forkJoinPool
        for (int i = 1; i < fileIndex; i++) {
            for (int j = 0; j < threadsIndex; j++) {
                Measure measure = new Measure();
                measure.setType(methods[2]);
                measure.setDepth(i);
                measure.setNbThreads(j * 30 + 1);
                forkJoinPoolMethod(measure);
                statisticMap.get(methods[2]).add(measure);
            }
        }

        Report report = new Report(statisticMap, methods);
        System.out.println(report.printStatistic());

        System.out.println("Generation des graphs");
        report.generateCharts();
    }

    private static void initialization(Measure stats){
        FileList fixtures = new FileList(stats.getDepth());
        List<File> fileList =  fixtures.getFileList();
        stats.setTotalFiles(fileList.size());

        treeBuilder = new TreeBuilder("root");
        fileMonitor = new MStructureMonitor<>();
        fileProducer = new FileProducer<>(fileMonitor);
        fileProducer.setThreshold(thresholdIndex);
        fileProducer.setFileList(fileList);
    }

    private static void sequentialMethod(Measure stats) throws IOException, InterruptedException {
        initialization(stats);

        printStatus(stats);
        stats.startWatch();

        //Version sequentielle - On produit tous les fichiers avant de continuer !
        Thread producerTh = new Thread(fileProducer);
        producerTh.start();
        producerTh.join();

        treeBuilder.build(new ArrayList<>(fileMonitor.getQueue()));
        stats.stopWatch();

        stats.setTotalNodes(NodeCount.countNodes(treeBuilder.getRoot()));

        stats.setDuplicates(getDuplicates());
        System.out.print("Done\n");
    }

    private static void threadsArrayMethod(Measure stats) throws IOException, InterruptedException {
        initialization(stats);

        printStatus(stats);
        stats.startWatch();

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();

        Thread[] threadsTree = new Thread[stats.getNbThreads()];

        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i] = new Thread(new TreeConsumer(fileMonitor, treeBuilder));
            threadsTree[i].start();
        }

        producerTh.join();

        for (int i = 0; i < stats.getNbThreads(); i++) {
            threadsTree[i].join();
        }

        stats.stopWatch();
        stats.setTotalNodes(NodeCount.countNodes(treeBuilder.getRoot()));

        Set<String> allItems = new HashSet<>();
        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());

        stats.setDuplicates(duplicates);
        System.out.print("Done\n");
    }


    private static void forkJoinPoolMethod(Measure stats) throws IOException, InterruptedException {
        initialization(stats);

        printStatus(stats);
        stats.startWatch();

        Thread producerTh = new Thread(fileProducer);
        producerTh.start();

        ForkJoinPool pool = new ForkJoinPool(stats.getNbThreads());
        for (int i = 0; i < stats.getNbThreads(); i++) {
            pool.execute(new TreeConsumer(fileMonitor, treeBuilder));
        }

        producerTh.join();
        pool.shutdown();

        stats.stopWatch();

        stats.setTotalNodes(NodeCount.countNodes(treeBuilder.getRoot()));

//        Configuration configuration = new Configuration();
//        FileSystemInterface fs = new FileSystemWrapperTest(configuration);

//        stats.startWatch();
//        Write Files in fs
//        WriterAction.compute(treeBuilder.getRoot(), fs);
//        stats.stopWatch();


//        stats.setTotalFilesWritten(FileCount.compute(fs, fs.getRootPath()));
//        Set<String> allItems = new HashSet<>();
//        Set<TreeNode> duplicates = treeBuilder.getNodes().stream()
//                .filter(n -> !allItems.add(n.getId()))
//                .collect(Collectors.toSet());

//        stats.setDuplicates(duplicates);
        System.out.print("Done\n");
    }


    /* Debugging and Utils */
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

    private static Set<TreeNode> getDuplicates(){
        Set<String> allItems = new HashSet<>();
        return treeBuilder.getNodes().stream()
                .filter(n -> !allItems.add(n.getId()))
                .collect(Collectors.toSet());
    }

    private static void printStatus(Measure measure){
        System.out.printf("Type: %-12s - TotalFiles: %-8d - NbThreads: %-3d ",
                measure.getType(), measure.getTotalFiles(), measure.getNbThreads());
    }
}
