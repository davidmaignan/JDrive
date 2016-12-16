package inf5171.stats;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by david on 2016-12-16.
 */
public class MeasureTest {
    private Measure measure;

    @Before
    public void setUp() throws Exception {
        measure = new Measure();
    }

    @Test
    public void formatTime() throws Exception {

        double[] numbers = new double[]{123.3284224000000, 2.10523224, 1234, 0.1, 0012.334};
        String[] expected = new String[]{"123.3284224000","2.1052322400", "1234.0000000000",
                "0.1000000000", "12.3340000000"};

        for (int i = 0; i < numbers.length; i++) {
            assertEquals(expected[i], measure.getFormattedElapsedTime(numbers[i]));
        }
    }
}