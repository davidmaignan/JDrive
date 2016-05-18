package database.labels;

import org.neo4j.graphdb.Label;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-13.
 */
public class ChangeLabel implements Label {
    @Override
    public String name() {
        return Labels.Change.toString();
    }
}
