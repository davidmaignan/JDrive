package org.drive;

import com.google.inject.Inject;
import org.config.Reader;

import java.nio.file.FileSystem;
import java.nio.file.Files;

/**
 * Local file
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class FileSearch {
    private String root;

    @Inject
    public FileSearch(Reader configReader){
        root = configReader.getRootFolder();
    }


    /**
     * Get absolute path
     * @param name
     * @return
     */
    public String getAbsolutePath(String name) {
        String absolutePath = null;

        java.io.File rootFile = new java.io.File(root);
        String filePath       = String.format("%s/%s", root, name);
        java.io.File file     = new java.io.File(filePath);

        if (file.exists()) {
            return file.getAbsolutePath();
        }

        return getAbsolutePath(rootFile, name);
    }

    /**
     * Get absolute path for a file from a folder
     * @param folder
     * @param name
     * @return
     */
    private String getAbsolutePath(java.io.File folder, String name) {
        String filePath   = String.format("%s/%s", folder.getAbsolutePath(), name);
        java.io.File file = new java.io.File(filePath);

        if(file.exists()) {
            return file.getAbsolutePath();
        }

        return getAbsolutePathRecursion(folder, name);
    }

    /**
     * Get recursively through directories and return file absolute path
     * @param folder
     * @param name
     * @return
     */
    private String getAbsolutePathRecursion(java.io.File folder, String name) {
        java.io.File[] fileList = folder.listFiles();

        for (java.io.File f : fileList) {
            if (f.isDirectory()){
                String r = getAbsolutePath(f, name);

                if (r != null && new java.io.File(r).exists()) {
                    return r;
                }
            }
        }

        return null;
    }
}
