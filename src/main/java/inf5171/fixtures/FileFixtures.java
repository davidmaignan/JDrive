package inf5171.fixtures;

import com.google.api.client.util.DateTime;
import com.google.gson.GsonBuilder;
import inf5171.JDriveMain_INF5171;
import inf5171.deserializer.DateTimeDeserializer;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by david on 2016-12-02.
 */
public class FileFixtures {

    private List<com.google.api.services.drive.model.File> fileList;

    public FileFixtures(String filename) throws IOException {
        fileList = getDataSet(filename);
    }

    public List<com.google.api.services.drive.model.File> getFileList() {
        return fileList;
    }

    public List<com.google.api.services.drive.model.File> getDataSet(String filename) throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        inf5171.fixtures.File[] fileList = gson.create().fromJson(new FileReader(
                        this.getClass().getClassLoader().getResource(filename).getFile()),
                inf5171.fixtures.File[].class
        );

        return Arrays.stream(fileList).map(f -> setFile(f)).collect(Collectors.toList());
    }

    private com.google.api.services.drive.model.File setFile(inf5171.fixtures.File f){
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();
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
}
