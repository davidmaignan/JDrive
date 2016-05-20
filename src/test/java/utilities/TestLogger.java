package utilities;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JDrive
 * Created by David Maignan <davidmaignan@gmail.com> on 15-09-08.
 */
public class TestLogger {
    @Test
    public void testLogger(){
        Logger logger = LoggerFactory.getLogger("Test logger");
        logger.info("Test logger 3");
    }
}
