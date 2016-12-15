package inf5171.stats;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by david on 2016-12-15.
 */
public class ReportTest {

    private String[] keys = new String[]{"sequential", "prod/con", "pool"};
    private Report report;

    @Before
    public void setUp() throws Exception {
        report = new Report(getDataSet());

    }

    @Test
    @Ignore
    public void getListOfTotalFiles() throws Exception {
        List<Integer> expected = Arrays.asList(new Integer[]{10, 110, 210, 310, 410});

        assertEquals(expected, report.getListOfTotalFiles());
    }

    @Test
    @Ignore
    public void getListOfNbThreads() throws Exception {
        List<Integer> expected = Arrays.asList(new Integer[]{0, 1, 2, 3, 4});

        assertEquals(expected, report.getListOfNbThreads());
    }

    @Test
    public void getAverageSequential() throws Exception {
        assertEquals(new Double(7), report.getAverageSequentialByStage(10, 0));
    }

    private Map<String, List<Measure>> getDataSet(){
        Map<String, List<Measure>> result = new HashMap<>();

        for (int i = 0; i < keys.length; i++) {
            result.put(keys[i], new ArrayList<>());
        }

        for (int k = 0; k < keys.length; k++) {
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 5; j++) {
                    Measure measure = new Measure();
                    measure.setDepth(i);
                    measure.setNbThreads(j);
                    measure.setType(keys[k]);
                    measure.setTotalFiles(i * 100 + 10);
                    measure.setTotalNodes(i * 100 + 10);
                    measure.setTotalFilesWritten(i * 10 + 10);

                    measure.setElapsedTime(0, 5 + j * 1);

                    result.get(keys[k]).add(measure);
                }
            }
        }

        return result;
    }

}