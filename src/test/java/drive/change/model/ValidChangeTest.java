package drive.change.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import database.repository.FileRepository;
import model.types.MimeType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by David Maignan <davidmaignan@gmail.com> on 2016-05-17.
 */
public class ValidChangeTest {

    private static Logger logger = LoggerFactory.getLogger(ValidChangeTest.class);

    ValidChange validChange;
    FileRepository fileRepository;
    Node fileNode;

    @Before
    public void setUp(){
        fileRepository = mock(FileRepository.class);
        fileNode = mock(Node.class);

        validChange = new ValidChange(fileRepository);
    }

    @Test
    public void testValidDeleteFileThatExists() throws Exception {
        Change change = this.getChange(true);

        when(fileRepository.getNodeById("file1")).thenReturn(fileNode);

        validChange.execute(change);

        assertTrue(validChange.isValid());
    }

    @Test
    public void testValidDeleteFileThatDoesNotExists() throws Exception {
        Change change = this.getChange(true);

        when(fileRepository.getNodeById("file1")).thenReturn(null);

        validChange.execute(change);

        assertFalse(validChange.isValid());
    }

    @Test
    public void testIsNewFile() throws Exception {
        Change change = this.getChange(false);

        when(fileRepository.getNodeById("file1")).thenReturn(null);

        validChange.execute(change);

        assertTrue(validChange.isNewFile());
    }

    @Test
    public void testIsValid(){
        Change change = this.getChange(false);

        when(fileRepository.getNodeById("file1")).thenReturn(fileNode);

        validChange.execute(change);

        assertTrue(validChange.isValid());
    }

    @Test
    public void testIsNotValidFileIsMissing(){
        Change change = this.getChange(false);

        change.setFile(null);

        when(fileRepository.getNodeById("file1")).thenReturn(fileNode);

        validChange.execute(change);

        assertFalse(validChange.isValid());
    }

    private Change getChange(boolean deleted){
        Change change = new Change();
        change.setId(1l);
        change.setModificationDate(new DateTime(1l));
        change.setDeleted(deleted);
        change.setSelfLink("mockSelfLink");
        change.setFileId("file1");

        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("file1");
        file1.setMimeType(MimeType.FOLDER);
        file1.setVersion(0l);

        file1.setParents(this.getParentReferenceList(
                "root",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));

        change.setFile(file1);

        return change;
    }

    private ArrayList<ParentReference> getParentReferenceList(String id, boolean bool) {
        ArrayList<ParentReference> parentList = new ArrayList<>();
        parentList.add(this.getParentReference(id, bool));

        return parentList;
    }

    private ParentReference getParentReference(String id, boolean bool) {
        ParentReference parentReference = new ParentReference();
        parentReference.setId(id);
        parentReference.setIsRoot(bool);

        return parentReference;
    }

    private ArrayList<User> getOwnerList(String displayName, boolean isAuthenticatedUser) {
        ArrayList<User> ownerList = new ArrayList<>();
        ownerList.add(this.getOwner(displayName, isAuthenticatedUser));

        return ownerList;
    }

    private User getOwner(String displayName, boolean isAuthenticatedUser) {
        User owner = new User();
        owner.setDisplayName(displayName);
        owner.setIsAuthenticatedUser(isAuthenticatedUser);

        return owner;
    }
}