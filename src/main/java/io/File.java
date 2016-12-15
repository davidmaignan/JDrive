package io;

import com.google.inject.Inject;
import drive.api.FileService;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Write a producer from a treeNode
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class File implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(File.class.getSimpleName());
    private FileService fileService;
    private FileSystemInterface fileSystem;
    private String fileId;

    @Inject
    public File(@Real FileSystemInterface fileSystem, FileService fileService) {
        this.fileSystem = fileSystem;
        this.fileService = fileService;
    }
    @Override
    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Override
    public FileSystemInterface getFileSystem() {
        return fileSystem;
    }

    @Override
    public boolean write(String pathString) {
        Path path = fileSystem.getRootPath().resolve(pathString);

        try(InputStream inputStream = fileService.downloadFile(this.fileId)) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);

            return true;

        } catch (IOException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }
}
