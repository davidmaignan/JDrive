package inf5171.stats;

import org.junit.Before;
import org.junit.Test;

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

        double[] numbers = new double[]{123.3284224, 2.10523224, 1234.284224, 1.1, 32412.334};

        for (int i = 0; i < numbers.length; i++) {
            System.out.println(measure.getFormattedElapsedTime(numbers[i]));
        }

    }

}