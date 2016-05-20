package io;

import io.filesystem.FileSystemInterface;

/**
 * I/O Writer interface
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public interface WriterInterface {
    public boolean write(String path);
    public boolean write(String oldPath, String newPath);
    public void setFileId(String fileId);
    public FileSystemInterface getFileSystem();
}
