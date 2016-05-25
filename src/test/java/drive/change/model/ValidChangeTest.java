package drive.change.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.gson.GsonBuilder;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import database.repository.FileRepository;
import fixtures.deserializer.DateTimeDeserializer;
import fixtures.extensions.FixturesInterface;
import model.types.MimeType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Node;
import org.neo4j.register.Register;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-17.
 */
@RunWith(DataProviderRunner.class)
public class ValidChangeTest implements FixturesInterface<fixtures.model.Change>{
    private static Logger logger = LoggerFactory.getLogger(ValidChangeTest.class.getSimpleName());
    private ValidChange validChange;
    private FileRepository fileRepository;
    private Node fileNode;


    private List<Change> list;

    @Before
    public void setUp() throws IOException {
        fileRepository = mock(FileRepository.class);
        fileNode = mock(Node.class);

        validChange = new ValidChange(fileRepository);

        list = getDataSet().stream().map(this::setChange).collect(Collectors.toList());
    }

    @Override
    public List<fixtures.model.Change> getDataSet() throws IOException {
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(DateTime.class, new DateTimeDeserializer());
        fixtures.model.Change[] fileList = gson.create().fromJson(new FileReader(
                        this.getClass().getClassLoader().getResource("fixtures/changes.json").getFile()),
                fixtures.model.Change[].class
        );

        return Arrays.asList(fileList);
    }

    private Change setChange(fixtures.model.Change c){
        Change change = new Change();
        change.setFileId(c.fileId);
        change.setKind(c.kind);
        change.setTime(c.time);
        change.setRemoved(c.removed);
        change.setFile(setFile(c.file));

        return change;
    }

    private File setFile(fixtures.model.File f){
        if(f == null)
            return null;

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
    @Test
    public void testValidDeleteFileThatExists() throws IOException {
        for(Change change : list){
            ValidChange validChange = new ValidChange(fileRepository);
            when(fileRepository.getNodeById(change.getFileId())).thenReturn(fileNode);
            validChange.execute(change);
            assertTrue(validChange.isValid());
        }
    }


    @Test
    public void testGetters() throws Exception {
        for(Change change : list){
            ValidChange validChange = new ValidChange(fileRepository);
            when(fileRepository.getNodeById(change.getFileId())).thenReturn(fileNode);
            validChange.execute(change);
            assertTrue(validChange.isValid());
            assertEquals(change, validChange.getChange());
            assertEquals(fileNode, validChange.getFileNode());
        }
    }

    @Test
    public void testValidDeleteFileThatDoesNotExists() throws Exception {
        //Check changes.json. The 5th one is removed = true.
        Change changeRemoved = list.remove(5);
        when(fileRepository.getNodeById(changeRemoved.getFileId())).thenReturn(null);
        validChange.execute(changeRemoved);
        assertFalse(validChange.isValid());
        assertFalse(validChange.isNewFile());
        
        for(Change change : list){
            ValidChange validChange = new ValidChange(fileRepository);
            when(fileRepository.getNodeById(change.getFileId())).thenReturn(null);
            validChange.execute(change);
            assertTrue(validChange.isValid());
            assertTrue(validChange.isNewFile());
        }
    }
}