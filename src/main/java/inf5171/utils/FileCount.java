package inf5171.utils;

import io.filesystem.FileSystemInterface;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Created by david on 2016-12-14.
 */
public class FileCount extends RecursiveTask<Integer> {

    private FileSystemInterface fs;
    private Integer total;
    private Path path;

    public FileCount(FileSystemInterface fs, Path path){
        this.fs = fs;
        this.path = path;
    }

    @Override
    public Integer compute() {

        total = 0;

        try {
            Files.list(path).forEach(file -> {
                try {
                    total += 1;
                    if(Files.isDirectory(file)){
                        FileCount fsc =  new FileCount(fs, file);
                        total += fsc.compute();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return total;
    }

    public static Integer compute(FileSystemInterface fs, Path path){
        return ForkJoinPool.commonPool().invoke(new FileCount(fs, path));
    }
}
