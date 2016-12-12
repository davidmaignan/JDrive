package inf5171.fixtures;

import com.google.api.client.util.DateTime;
import com.google.gson.GsonBuilder;
import inf5171.deserializer.DateTimeDeserializer;
import model.types.MimeType;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by david on 2016-12-02.
 */
public class FileFixtures {

    private List<com.google.api.services.drive.model.File> fileList;
    private int total;

    public FileFixtures(int total){
        this.total = total;
        fileList = getDataSet();
    }

    public FileFixtures(String filename) throws IOException {
        fileList = getDataSet(filename);
    }

    public List<com.google.api.services.drive.model.File> getDataSet(){
        List<com.google.api.services.drive.model.File> fileList = new ArrayList<>();

        String parentId = "root";
        String[] parentIds = new String[]{"root","root","root","root","root"};

        for (int i = 0; i < total; i++) {
            String name = "folder_" + i;

            fileList.add(createFile(name, "root", MimeType.FOLDER));
            Collections.addAll(fileList, generateFiles(name));

            for (int j = 0; j < total; j++) {
                String sub_1 = name + j;

                fileList.add(createFile(sub_1, name, MimeType.FOLDER));
                Collections.addAll(fileList, generateFiles(sub_1));


                for (int k = 0; k < total; k++) {
                    String sub_2 = sub_1 + k;

                    fileList.add(createFile(sub_2, sub_1, MimeType.FOLDER));
                    Collections.addAll(fileList, generateFiles(sub_2));

                    for (int l = 0; l < total; l++) {
                        String sub_3 = sub_2 + l;

                        fileList.add(createFile(sub_3, sub_2, MimeType.FOLDER));
                        Collections.addAll(fileList, generateFiles(sub_3));

                        for (int m = 0; m < total; m++) {
                            String sub_4 = sub_3 + m;

                            fileList.add(createFile(sub_4, sub_3, MimeType.FOLDER));
                            Collections.addAll(fileList, generateFiles(sub_4));
                        }
                    }
                }
            }
        }

        return fileList;
    }

    private com.google.api.services.drive.model.File[] generateFiles(String parent){
        com.google.api.services.drive.model.File[] files = new com.google.api.services.drive.model.File[5];

        for (int i = 0; i < 5; i++) {
            String filename = parent.replaceFirst("folder", "file")+ i;
            files[i] = createFile(filename, parent, MimeType.DOCUMENT);
        }

        return files;
    }



    private String generateName(String prefix, int id){
        StringBuilder builder = new StringBuilder(prefix);

        builder.append(id);

        return builder.toString();
    }

    public com.google.api.services.drive.model.File createFile(String name, String parent, String type){
        com.google.api.services.drive.model.File file = new com.google.api.services.drive.model.File();

        file.setId(name);
        file.setName(name);
        file.setMimeType(type);
        file.setTrashed(false);
        file.setParents(getParents(parent));
        file.setVersion(18608L);
        file.setCreatedTime(DateTime.parseRfc3339("2016-05-11T17:22:20.741Z"));
        file.setModifiedTime(DateTime.parseRfc3339("2016-05-11T17:22:20.741Z"));

        return file;
    }

    public List<String> getParents(String id){
        return Arrays.asList(new String[]{id});
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
