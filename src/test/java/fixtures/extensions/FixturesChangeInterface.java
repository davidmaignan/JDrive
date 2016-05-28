package fixtures.extensions;

import java.io.IOException;
import java.util.List;

/**
 * Created by david on 2016-05-25.
 */
public interface FixturesChangeInterface<T> {
    List<T> getChangeSet() throws IOException;
}

