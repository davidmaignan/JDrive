package fixtures.model;

import com.google.api.client.util.DateTime;

/**
 * Created by david on 2016-05-24.
 */
public class Change {
    public String kind;
    public String fileId;
    public boolean removed;
    public DateTime time;
    public File file;
}