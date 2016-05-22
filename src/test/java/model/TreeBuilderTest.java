package model;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.gson.GsonBuilder;
import fixtures.deserializer.DateTimeDeserializer;
import fixtures.extensions.Test_Database_Extensions;
import model.tree.TreeNode;
import org.junit.Before;
import model.tree.TreeBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class TreeBuilderTest extends Test_Database_Extensions<fixtures.model.File> {
    private static Logger logger = LoggerFactory.getLogger(TreeBuilderTest.class.getSimpleName());
    private TreeBuilder treeBuilder;

    @Before
    public void setUp() throws IOException {
        //@todo Mock reader object injected
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

    @Override
    protected List<fixtures.model.File> getDataSet() throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        fixtures.model.File[] fileList = gson.create().fromJson(new FileReader(
                this.getClass().getClassLoader().getResource("fixtures/files.json").getFile()),
                fixtures.model.File[].class
        );

        return Arrays.asList(fileList);
    }

    private File setFile(fixtures.model.File f){
        File file = new File();
        file.setId(f.id);
        file.setName(f.name);
        file.setMimeType(f.mimeType);
        file.setTrashed(f.trashed);
        file.setParents(f.parents);
        file.setVersion(f.version);
        file.setCreatedTime(f.createdTime);
        file.setModifiedTime(f.modifiedTime);

        return file;
    }
}