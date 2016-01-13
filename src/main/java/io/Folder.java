package io;

import org.neo4j.graphdb.Node;

import java.io.File;

public class Folder implements WriterInterface{

    private Node node;

    @Override
    public boolean write(String path) {
        File file = new File(path);

        return file.mkdir();
    }

    @Override
    public void setNode(Node node) {
        this.node = node;
    }

    @Override
    public boolean delete(String path) {
        File file = new File(path);

        return file.delete();
    }
}
