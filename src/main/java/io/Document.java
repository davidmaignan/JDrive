package io;

import com.google.inject.Inject;
import database.repository.FileRepository;
import model.tree.TreeNode;
import org.api.DriveService;
import org.api.FileService;
import org.neo4j.graphdb.Node;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-19.
 */
public class Document implements WriterInterface {
    private Node node;
    private String id;
    private File file;
    private PrintWriter output;

    private final DriveService driveService;
    private final FileService fileService;
    private final FileRepository fileRepository;

    @Inject
    public Document(DriveService driveService, FileRepository fileRepository, FileService fileService) {
        this.driveService = driveService;
        this.fileRepository = fileRepository;
        this.fileService = fileService;
    }

    @Override
    public boolean write(String path) {
        try{
            file      = new File(path);
            output    = new PrintWriter(file);
            output.write(this.setContent());
            output.close();
        } catch (IOException e) {
            return false;
        }

        return true;
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

    @Override
    public void setNode(Node node) {
        this.node = node;

        this.id = fileRepository.getFileId(node);
    }

    @Override
    public boolean delete(String path) {
        return false;
    }
}
