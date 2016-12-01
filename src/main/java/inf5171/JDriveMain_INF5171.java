package inf5171;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.gson.GsonBuilder;
import inf5171.deserializer.DateTimeDeserializer;
import model.tree.TreeBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by david on 2016-12-01.
 */
public class JDriveMain_INF5171 {
    public static void main(String args[]) throws IOException {
        List<File> list = new ArrayList<>();

        for(inf5171.fixtures.File file : getDataSet("fixtures/files.json")){
            list.add(setFile(file));
        }

        TreeBuilder treeBuilder = new TreeBuilder("root");
        treeBuilder.build(list);
    }

    protected static File setFile(inf5171.fixtures.File f){
        File file = new File();
        file.setId(f.id);
        file.setName(f.name);
        file.setMimeType(f.mimeType);
        file.setTrashed(f.trashed);
        file.setParents(f.parents);
        file.setVersion(f.version);
        file.setCreatedTime(f.createdTime);
        file.setModifiedTime(f.modifiedTime);

        return file;
    }

    public static List<inf5171.fixtures.File> getDataSet(String filename) throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        inf5171.fixtures.File[] fileList = gson.create().fromJson(new FileReader(
                        JDriveMain_INF5171.class.getClassLoader().getResource(filename).getFile()),
                inf5171.fixtures.File[].class
        );

        return Arrays.asList(fileList);
    }
}
