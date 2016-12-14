package model;

import com.google.api.services.drive.model.File;
import fixtures.extensions.TestDatabaseExtensions;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import model.types.MimeType;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderTest extends TestDatabaseExtensions {
    private static Logger logger = LoggerFactory.getLogger(TreeBuilderTest.class.getSimpleName());
    private TreeBuilder treeBuilder;

    @Before
    public void setUp() throws IOException {
        treeBuilder = new TreeBuilder("0AHmMPOF_fWirUk9PVA");
        List<File> list = new ArrayList<>();

        for(fixtures.model.File file : getDataSet()){
            list.add(setFile(file));
        }

        treeBuilder.build(list);
    }

    @Test(timeout = 1000)
    public void testGetRoot() throws Exception {
        assertTrue(treeBuilder.getRoot().isRoot());
        assertEquals("0AHmMPOF_fWirUk9PVA", treeBuilder.getRoot().getId());
    }

    @Test(timeout = 1000)
    public void testDirectoryStructure(){
        assertEquals(5, treeBuilder.getRoot().getChildren().size());
        assertEquals("Air Canada", treeBuilder.getRoot().getChildren().remove().getName());

        TreeNode folder1 = treeBuilder.getRoot().getChildren().remove();

        assertEquals("folder1", folder1.getName());
        assertEquals(1, folder1.getChildren().size());
    }

    @Test(timeout = 1000)
    public void getFilePath() throws Exception{
        TreeNode root = treeBuilder.getRoot();

        assertEquals("/Air Canada", root.getChildren().remove().getAbsolutePath());

        TreeNode file = root.getChildren().remove().getChildren().remove();
        assertEquals("/folder1/file11.png", file.getAbsolutePath());

        TreeNode folder = root.getChildren().remove();
        assertEquals("/folder", folder.getAbsolutePath());
        assertEquals("/folder/c3po.jpg", folder.getChildren().remove().getAbsolutePath());
        assertEquals("/folder/.DS_Store", folder.getChildren().remove().getAbsolutePath());

        TreeNode file2 = folder.getChildren().remove().getChildren().remove();
        assertEquals("/folder/first/destination.csf", file2.getAbsolutePath());
    }

    @Test(timeout = 1000)
    public void insertFile() throws Exception{
        TreeBuilder treeBuilder = new TreeBuilder("root");

        assertNotNull(treeBuilder.insertFile(createFile("file_0", "root", MimeType.DOCUMENT)));
        assertNotNull(treeBuilder.insertFile(createFile("folder_0", "root", MimeType.FOLDER)));
        assertNotNull(treeBuilder.insertFile(createFile("file_00", "folder_0", MimeType.FOLDER)));
        assertNull(treeBuilder.insertFile(createFile("file_00", "folder_1", MimeType.FOLDER)));
    }

    private File createFile(String filename, String parentId, String type){
        File file = new File();

        file.setId(filename);
        file.setName(filename);
        file.setMimeType(type);
        file.setTrashed(false);
        file.setVersion(1l);
        file.setParents(getParents(parentId));

        return file;
    }

    private List<String> getParents(String parentId){
        return Arrays.asList(new String[]{parentId});
    }


}