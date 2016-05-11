package io;

import org.neo4j.graphdb.Node;

/**
 * Node Writer interface
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface WriterInterface {
    public boolean write(String path);
    public void setFileId(String fileId);
    public boolean delete(String path);
}
