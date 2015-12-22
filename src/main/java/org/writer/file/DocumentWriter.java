package org.writer.file;

import com.google.inject.Inject;
import org.api.FileService;
import org.writer.WriterInterface;

import java.io.PrintWriter;

/**
 * Document writer
 *
 * Create / Update file with google drive content information
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class DocumentWriter implements WriterInterface {
    private FileService fileService;
//    private Vertex vertex;
    private com.google.api.services.drive.model.File file;
    private PrintWriter output;

    @Inject
    public DocumentWriter(FileService fileService) {
        this.fileService = fileService;
    }

//    public void setVertex(Vertex vertex){
//        this.vertex = vertex;
//    }

    @Override
    public boolean write() {
//        com.google.api.services.drive.model.File file = this.fileService.getFile(vertex.getProperty(Fields.ID).toString());
//
//        try{
//            File fileIO  = new File(vertex.getProperty(Fields.PATH).toString());
//            output       = new PrintWriter(fileIO);
//            output.write(this.setContent(file));
//            output.close();
//        } catch (IOException e) {
//            return false;
//        }

        return true;
    }

    private String setContent(com.google.api.services.drive.model.File file) {
        return String.format("" +
                "{\"url\": \"https://docs.google.com/open?id=%s\", " +
                "\"doc_id\": \"%s\", \"email\": " +
                "\"%s\", \"resource_id\": \"document:%s\"})",
                file.getId(),
                file.getId(),
                file.getOwners().get(0).getEmailAddress(),
                file.getId()
        );
    }
}
