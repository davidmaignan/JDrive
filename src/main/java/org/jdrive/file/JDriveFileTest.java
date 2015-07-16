package org.jdrive.file;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.model.File;
import org.junit.Test;
import org.model.TreeBuilder;
import org.model.TreeNode;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * JUnit DriveFile
 */
public class JDriveFileTest {

    @Test(timeout = 1000)
    public void getFilePath(){
        ArrayList<File>listFile = new ArrayList<>();

        com.google.api.services.drive.model.File folder1 = new File();

        folder1.setTitle("folder1");
        folder1.setId("0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE");
        folder1.setMimeType("application/vnd.google-apps.folder");
        folder1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));

        folder1.setParents(this.getParentReferenceList(
                "0AHRgC7jH8BP_Uk9PVA", true
        ));

        folder1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder1);

        com.google.api.services.drive.model.File file1 = new com.google.api.services.drive.model.File();
        file1.setTitle("file1");
        file1.setId("1K2T_qDWBhlyk_OVL9Q6JYmQZVcIo-Y9HSKz54RAMPhM");
        file1.setMimeType("application/vnd.google-apps.document");

        file1.setParents(this.getParentReferenceList(
                "0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file1);

        TreeBuilder treeBuilder = new TreeBuilder(listFile);
        TreeNode root = treeBuilder.getRoot();


        JDriveFile file = new JDriveFile(root.getChildren().get(0).getChildren().get(0));

        assertEquals("/root/folder1/file1", file.getAbsolutePath());
    }

    /**
     * Get a parentReferenceList
     * @param id id of the parent file
     * @param bool true if root
     * @return list of parent reference
     */
    private ArrayList<ParentReference> getParentReferenceList(String id, boolean bool){
        ArrayList<ParentReference> parentList = new ArrayList<>();
        parentList.add(this.getParentReference(id, bool));

        return parentList;
    }

    /**
     * Get a parentReference
     * @param id id of the parent file
     * @param bool true if root
     * @return parentReference
     */
    private ParentReference getParentReference(String id, boolean bool){
        ParentReference parentReference = new ParentReference();
        parentReference.setId(id);
        parentReference.setIsRoot(bool);

        return parentReference;
    }

    /**
     * Get a ownerlist
     * @param displayName owner name of the file
     * @param isAuthenticatedUser true if own by the user
     * @return list of owners
     */
    private ArrayList<User> getOwnerList(String displayName, boolean isAuthenticatedUser) {
        ArrayList<User> ownerList = new ArrayList<>();
        ownerList.add(this.getOwner(displayName, isAuthenticatedUser));

        return ownerList;
    }

    /**
     * Get a owner
     * @param displayName owner name of the file
     * @param isAuthenticatedUser true if own by the user
     * @return owner of the file
     */
    private User getOwner(String displayName, boolean isAuthenticatedUser) {
        User owner = new User();
        owner.setDisplayName(displayName);
        owner.setIsAuthenticatedUser(isAuthenticatedUser);

        return owner;
    }
}