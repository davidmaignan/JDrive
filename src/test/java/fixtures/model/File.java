package fixtures.model;

import com.google.api.client.util.DateTime;

import java.util.ArrayList;

/**
 * Created by david on 2016-05-22.
 */
public class File {
    public String id;
    public String name;
    public String mimeType;
    public boolean trashed;
    public boolean explicitlyTrashed;
    public ArrayList<String> parents;
    public Long version;
    public DateTime createdTime;
    public DateTime modifiedTime;

    @Override
    public String toString() {
        return "File{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", trashed=" + trashed +
                ", explicitlyTrashed=" + explicitlyTrashed +
                ", parents=" + parents +
                ", version=" + version +
                ", createdTime=" + createdTime +
                ", modifiedTime=" + modifiedTime +
                '}';
    }
}