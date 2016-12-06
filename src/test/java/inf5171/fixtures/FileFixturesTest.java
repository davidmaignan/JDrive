package inf5171.fixtures;

import model.tree.TreeBuilder;
import org.junit.Before;
import org.junit.Test;
import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * Load fixtures from a json file
 *
 * Structure:
 *  - root
 *    - (folder|file)_[0-9]{1}
 *      - (folder|file)_[0-9]{2,} (a new digit gets added
 *
 * Created by david on 2016-12-02.
 */
public class FileFixturesTest {
    private FileFixtures fileFixtures;
    private String filename = "fixtures/inf5171/files.json";
    private Pattern patterns[];
    private String patternList[];
    private String rootName = "root";
    private TreeBuilder treeBuilder;


    @Before
    public void setUp() throws Exception {
        fileFixtures = new FileFixtures();

        patternList = new String[]{ "folder_([0-9])+", "file_([0-9])+"};
        patterns = new Pattern[patternList.length];

        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = Pattern.compile(patternList[i]);
        }

        treeBuilder = new TreeBuilder(rootName);
    }

    @Test
    public void testPatternMatch(){
        String[] testList = new String[]{"root", "folder_0", "file_00", "folder_123", "file_4444"};
        String[] expected = new String[]{"", "root", "folder_0", "folder_12", "folder_444"};

        for (int i = 0; i < testList.length; i++) {
            assertEquals(expected[i], parentExpected(testList[i]));
        }
    }

    @Test
    public void getDataSet() throws Exception {
        List<File> fileList = fileFixtures.getDataSet(filename);

        assertEquals(60, fileList.size());

        for (File file: fileList) {
            assertEquals(parentExpected(file.getId()), file.getParents().get(0));
        }
    }

    @Test
    public void testStructure() throws IOException {
        List<File> fileList = fileFixtures.getDataSet(filename);

        treeBuilder.build(fileList);

        System.out.println(treeBuilder.getRoot().getChildren().get(0).getChildren().get(0).getAbsolutePath());
    }

    private String parentExpected(String name){
        for (int i = 0; i < patterns.length; i++) {
            Matcher matcher = patterns[i].matcher(name);

            if(matcher.matches()){
                String result = name.replaceFirst("file", "folder");

                // Only one digit at the end - the parent is root
                if( ! result.matches(".*\\d{2,}$")){
                    return rootName;
                }

                return result.substring(0, result.length()-1);
            }
        }

        return "";
    }
}