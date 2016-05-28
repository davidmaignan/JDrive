package model;

import com.google.api.services.drive.model.File;
import fixtures.extensions.TestDatabaseExtensions;
import model.tree.TreeBuilder;
import model.tree.TreeNode;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        assertEquals("Air Canada", treeBuilder.getRoot().getChildren().get(0).getName());

        TreeNode folder1 = treeBuilder.getRoot().getChildren().get(1);

        assertEquals("folder1", folder1.getName());
        assertEquals(1, folder1.getChildren().size());
    }

    @Test(timeout = 1000)
    public void getFilePath() throws Exception{
        TreeNode root = treeBuilder.getRoot();

        assertEquals("/Air Canada", root.getChildren().get(0).getAbsolutePath());
        TreeNode file = treeBuilder.getRoot().getChildren().get(1).getChildren().get(0);

        assertEquals("/folder1/file11.png", file.getAbsolutePath());

        TreeNode file2 = treeBuilder.getRoot().getChildren().get(2).getChildren().get(2).getChildren().get(0);
        assertEquals("/folder/first/destination.csf", file2.getAbsolutePath());
    }
}