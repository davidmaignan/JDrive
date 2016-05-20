package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import drive.api.DriveService;
import drive.api.FileService;
import io.filesystem.FileSystemInterface;
import io.filesystem.annotations.Real;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.*;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class Document implements WriterInterface {
    private static Logger logger = LoggerFactory.getLogger(Document.class.getSimpleName());
    private FileSystemInterface fileSystem;
    private Path path;
    private String id;

    @Inject
    public Document(@Real FileSystemInterface fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean write(String pathString) {
        path = this.fileSystem.getPath(pathString);
        byte data[] = setContent().getBytes(StandardCharsets.UTF_8);
        try (
                OutputStream out = new BufferedOutputStream(
                Files.newOutputStream(path, CREATE, APPEND))
        ) {
            out.write(data, 0, data.length);
        } catch (IOException exception) {
            logger.error(exception.getMessage());
        }

        return true;
    }

    @Override
    public boolean write(String oldPath, String newPath) {
        return false;
    }

    @Override
    public void setFileId(String fileId) {

    }

    private String setContent() {
        return String.format("" +
                "{\"url\": \"https://docs.google.com/open?id=%s\", " +
                "\"doc_id\": \"%s\", \"email\": " +
                "\"%s\", \"resource_id\": \"document:%s\"})",
                this.id,
                this.id,
                "",
                this.id
        );
    }
}
