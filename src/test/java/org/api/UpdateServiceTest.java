package org.api;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import org.db.Fields;
import org.db.neo4j.DatabaseService;
import org.io.ChangeInterface;
import org.io.DeleteService;
import org.io.ModifiedService;
import org.io.MoveService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import org.model.types.MimeType;
import org.neo4j.graphdb.*;
import org.writer.FactoryProducer;

import java.util.ArrayList;

public class UpdateServiceTest {
    private DatabaseService spyDbService;
    private FileService spyFileService;
    private FactoryProducer spyFactoryProducer;
    private UpdateService service;
    private ChangeInterface deleteService;
    private ChangeInterface moveService;
    private ChangeInterface modifiedService;

    @Before
    public void setUp() {
        DatabaseService dbService = new DatabaseService();
        FileService fileService   = new FileService();
        deleteService             = new DeleteService();
        moveService               = new MoveService();
        modifiedService           = new ModifiedService();

        spyDbService              = spy(dbService);
        spyFileService            = spy(fileService);
        spyFactoryProducer        = spy(FactoryProducer.class);

        service                   = new UpdateService(
                spyDbService, spyFileService, deleteService,
                moveService, modifiedService, spyFactoryProducer
        );
    }

    @Test
    public void testModificationDateOlder() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path/folder1",
                MimeType.FOLDER,
                "1420643650751",
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);

        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-01-06T15:14:10.751Z"));
        ChangeInterface changeService = service.update(change);

        assertNull(changeService);
    }

    @Test
    public void testChangeDeleteTrue() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path/folder1",
                MimeType.FOLDER,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);

        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));
        change.setDeleted(true);
        ChangeInterface changeService = service.update(change);

        assertTrue(changeService instanceof DeleteService);
    }

    @Test
    public void testFileSetExplicitelyTrashed() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path/folder1",
                MimeType.FOLDER,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);

        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));

        File file = new File();
        file.setExplicitlyTrashed(true);
        change.setFile(file);

        ChangeInterface changeService = service.update(change);

        assertTrue(changeService instanceof DeleteService);
    }

    @Test
    public void testSetLabelTrash() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path/folder1",
                MimeType.FOLDER,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);

        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));

        File file = new File();
        file.setLabels(new File.Labels().setTrashed(true));
        change.setFile(file);

        ChangeInterface changeService = service.update(change);

        assertTrue(changeService instanceof DeleteService);
    }

    @Test
    public void testChangeFolderRoot() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path/folder1",
                MimeType.FOLDER,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "true"
        };

        Node node = this.getNodeMocked(args);

        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));

        File file = new File();
        file.setLabels(new File.Labels().setTrashed(false));
        file.setMimeType(MimeType.FOLDER);
        change.setFile(file);

        ChangeInterface changeService = service.update(change);

        assertNull(changeService);
    }

    @Test
    public void testChangeFileMove() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path1/folder1",
                MimeType.FILE,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);
        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        //Mock parent node
        String[] argsParent = new String[] {
                "mockPath1",
                "path1",
                "/mock/path1/",
                MimeType.FOLDER,
                "1420643650751",
                "1420643650751",
                "false"
        };

        Node parentNode = this.getNodeMocked(argsParent);
        when(spyDbService.getParent("mockID")).thenReturn(parentNode);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));

        File file = new File();
        file.setLabels(new File.Labels().setTrashed(false));
        file.setMimeType(MimeType.FOLDER);
        file.setParents(this.getParentReferenceList(
                "mockPath2",
                false
        ));

        change.setFile(file);

        ChangeInterface changeService = service.update(change);

        assertTrue(changeService instanceof MoveService);
    }

    @Test
    public void testModifiedDateWithoutFileMove() throws Exception {
        String[] args = new String[]{
                "mockID",
                "folder1",
                "/mock/path1/folder1",
                MimeType.FILE,
                "1420643650751", //"2015-01-06T15:14:10.751Z"
                "1420643650751",
                "false"
        };

        Node node = this.getNodeMocked(args);
        when(spyDbService.getNodeById("mockID")).thenReturn(node);

        //Mock parent node
        String[] argsParent = new String[] {
                "mockPath1",
                "path1",
                "/mock/path1/",
                MimeType.FOLDER,
                "1420643650751",
                "1420643650751",
                "false"
        };

        Node parentNode = this.getNodeMocked(argsParent);
        when(spyDbService.getParent("mockID")).thenReturn(parentNode);

        Change change = new Change();
        change.setFileId("mockID");
        change.setModificationDate(new DateTime("2015-02-06T15:14:10.751Z"));

        File file = new File();
        file.setLabels(new File.Labels().setTrashed(false));
        file.setMimeType(MimeType.FOLDER);
        file.setParents(this.getParentReferenceList(
                "mockPath1",
                false
        ));

        change.setFile(file);

        ChangeInterface changeService = service.update(change);

        assertTrue(changeService instanceof ModifiedService);
    }


    private ArrayList<ParentReference> getParentReferenceList(String id, boolean bool){
        ArrayList<ParentReference> parentList = new ArrayList<>();
        parentList.add(this.getParentReference(id, bool));

        return parentList;
    }

    private ParentReference getParentReference(String id, boolean bool){
        ParentReference parentReference = new ParentReference();
        parentReference.setId(id);
        parentReference.setIsRoot(bool);

        return parentReference;
    }


    /**
     * Get node mock
     *
     * @return Node
     */
    private Node getNodeMocked(String[] args){
        Node node = mock(Node.class);

        when(node.getProperty(Fields.ID)).thenReturn(args[0]);
        when(node.getProperty(Fields.TITLE)).thenReturn(args[1]);
        when(node.getProperty(Fields.PATH)).thenReturn(args[2]);
        when(node.getProperty(Fields.MIME_TYPE)).thenReturn(args[3]);
        when(node.getProperty(Fields.CREATED_DATE)).thenReturn(args[4]);
        when(node.getProperty(Fields.MODIFIED_DATE)).thenReturn(args[5]);
        when(node.getProperty(Fields.IS_ROOT)).thenReturn(args[6]);

        return node;
    }
}
