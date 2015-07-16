package org.jdrive.file;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import org.config.Reader;
import org.junit.Before;
import org.junit.Test;
import org.model.TreeNode;

import java.util.ArrayList;


import static org.junit.Assert.*;

public class WriterTest {

    private Reader reader;

    @Before
    public void setUp() throws Exception {
        reader = new Reader();
    }

    @Test
    public void testFileExists() throws Exception{
        File file = new File();
        file.setTitle(reader.getProperty("rootFolder"));
        file.setParents(new ArrayList<ParentReference>());
        file.setOwners(new ArrayList<User>());
        file.setMimeType("application/vnd.google-apps.folder");

        TreeNode root = new TreeNode(file);

        Writer fileWriter = new Writer(root);

        assertFalse(fileWriter.exist());
    }

    @Test
    public void testFileCreation() throws Exception {
        File file = new File();
        file.setTitle(reader.getProperty("rootFolder"));
        file.setParents(new ArrayList<ParentReference>());
        file.setOwners(new ArrayList<User>());
        file.setMimeType("application/vnd.google-apps.folder");

        TreeNode root = new TreeNode(file);

        Writer fileWriter = new Writer(root);

        fileWriter.write();

        assertTrue(fileWriter.exist());
    }
}