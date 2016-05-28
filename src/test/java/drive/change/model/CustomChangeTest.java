package drive.change.model;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

/**
 * Created by david on 2016-05-27.
 */
public class CustomChangeTest {
    private static Logger logger = LoggerFactory.getLogger(CustomChangeTest.class.getSimpleName());

    private List<CustomChange> customChangeList;

    @Before
    public void setUp() throws Exception {
        customChangeList = new ArrayList<>();
    }

    @Test
    public void testSortingList(){
        CustomChange customChange1 = new CustomChange();
        customChange1.setType(ChangeTypes.DELETE);

        ChangeTypes list[] = new ChangeTypes[]{
                ChangeTypes.VERSION,
                ChangeTypes.NULL,
                ChangeTypes.DELETE,
                ChangeTypes.NEW_FOLDER,
                ChangeTypes.NEW_FOLDER,
                ChangeTypes.TRASHED,
                ChangeTypes.UNTRASHED,
                ChangeTypes.NEW_FILE,
                ChangeTypes.NEW_FILE,
                ChangeTypes.FILE_UPDATE,
                ChangeTypes.FOLDER_UPDATE,
                ChangeTypes.DOCUMENT,
                ChangeTypes.MOVE,
                ChangeTypes.MOVE
        };

        Long[] listDepth = new Long[]{10L , 12L, 1L, 3L, 4L, 5L, 10L, 3L, 7L, 3L, 2L, 1L, 5L, 2L};


        customChangeList = generateList(list, listDepth);

        Collections.sort(customChangeList);

        ChangeTypes expected[] = new ChangeTypes[]{
                ChangeTypes.NEW_FOLDER,
                ChangeTypes.NEW_FOLDER,
                ChangeTypes.NEW_FILE,
                ChangeTypes.NEW_FILE,
                ChangeTypes.UNTRASHED,
                ChangeTypes.DOCUMENT,
                ChangeTypes.FOLDER_UPDATE,
                ChangeTypes.FILE_UPDATE,
                ChangeTypes.MOVE,
                ChangeTypes.MOVE,
                ChangeTypes.DELETE,
                ChangeTypes.TRASHED,
                ChangeTypes.VERSION,
                ChangeTypes.NULL
        };
        Long[] listDepthExpected = new Long[]{3L, 4L, 3L, 7L, 10L, 1L, 2L, 3L, 2L, 5L, 1L, 5L, 10L, 12L};

        assertArrayEquals(customChangeList.toArray(), generateList(expected, listDepthExpected).toArray());
    }

    private List<CustomChange> generateList(ChangeTypes[] expected, Long[] depths){
        List<CustomChange> customChangeList = new ArrayList<>();

        for(int i = 0; i < expected.length; i++)
            customChangeList.add(generateCustomChange(expected[i], depths[i]));

        return customChangeList;
    }

    private CustomChange generateCustomChange(ChangeTypes type, Long depth){
        CustomChange customChange = new CustomChange();
        customChange.setType(type);
        customChange.setDepth(depth);

        return customChange;
    }
}