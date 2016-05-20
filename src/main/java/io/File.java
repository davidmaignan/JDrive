package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.api.DriveService;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.*;
import java.nio.*;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.APPEND;

/**
 * Write a file from a treeNode
 * <p>
 * David Maignan <davidmaignan@gmail.com>
 */
public class File implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(File.class.getSimpleName());
    private DriveService driveService;
    private FileSystemInterface fileSystem;
    private String fileId;

    @Inject
    public File(@Real FileSystemInterface fileSystem, DriveService driveService) {
        this.fileSystem = fileSystem;
        this.driveService = driveService;
    }
    @Override
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public boolean write(String pathString) {
        Path path = fileSystem.getPath(pathString);
        try{
            InputStream inputStream   = this.downloadFile(driveService, fileId);
            OutputStream outputStream = new BufferedOutputStream(
                    Files.newOutputStream(path, CREATE, APPEND));

            if (inputStream == null) {
                return false;
            }

            int r;

            while ((r = inputStream.read()) != -1) {
                outputStream.write((byte) r);
            }

            return true;

        } catch (IOException exception){
            exception.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }

    private InputStream downloadFile(DriveService service, String id) throws IOException {
        try {
            return driveService.getDrive().files().get(id).executeMediaAsInputStream();
        } catch (IOException e) {
            logger.error("Cannot get file from google drive api: " + id);
            return null;
        }
    }
}
