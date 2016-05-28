package io;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class Document implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(Document.class.getSimpleName());
    private FileSystemInterface fileSystem;
    private Path path;
    private String id;
    private String content;

    @Inject
    public Document(@Real FileSystemInterface fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean write(String pathString) {
        path = this.fileSystem.getRootPath().resolve(pathString);

        content = setContent();

        try{
            Files.write(path, ImmutableList.of(content), StandardCharsets.UTF_8);
            return true;
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }


        return false;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }

    @Override
    public void setFileId(String fileId) {

    }

    public FileSystemInterface getFileSystem(){
        return fileSystem;
    }

    private String setContent() {
        return String.format("{\"url\": \"https://docs.google.com/open?id=%s\", " +
                "\"doc_id\": \"%s\", \"email\": " +
                "\"%s\", \"resource_id\": \"document:%s\"}",
                this.id,
                this.id,
                "",
                this.id
        );
    }

    public String getContent(){
        return content;
    }
}
