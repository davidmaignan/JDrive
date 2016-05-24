package fixtures.extensions;

import java.io.IOException;
import java.util.List;

/**
 * Created by david on 2016-05-24.
 */
public interface FixturesInterface<T> {
    public abstract List<T> getDataSet() throws IOException;
}
