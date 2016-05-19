package io;

/**
 * I/O Writer interface
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface WriterInterface {
    public boolean write(String path);
    public void setFileId(String fileId);
}
