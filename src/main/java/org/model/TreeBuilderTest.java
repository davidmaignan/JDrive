package org.model;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.drive.model.User;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.util.List;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TreeBuilderTest {

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

    @Test(timeout = 1000)
    public void testGetRoot() throws Exception {
        List<File> liste    = new ArrayList<>();
        TreeBuilder treeBuilder = new TreeBuilder(liste);

        TreeNode root = treeBuilder.getRoot();
        assertTrue(root.isSuperRoot());
        assertEquals(null, root.getId());
    }

    @Test(timeout = 1000)
    public void testDirectoryStructure() throws Exception {

        ArrayList<File>listFile = new ArrayList<>();

        File folder1 = new File();

        folder1.setTitle("folder1");
        folder1.setId("0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE");
        folder1.setMimeType("application/vnd.google-apps.folder");
        folder1.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));

        folder1.setParents(this.getParentReferenceList(
                "0AHRgC7jH8BP_Uk9PVA", true
        ));

        folder1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder1);

        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("1K2T_qDWBhlyk_OVL9Q6JYmQZVcIo-Y9HSKz54RAMPhM");
        file1.setMimeType("application/vnd.google-apps.document");

        file1.setParents(this.getParentReferenceList(
                "0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE",
                false
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file1);

        File folder2 = new File();
        folder2.setTitle("folder2");
        folder2.setId("0B3mMPOF_fWirfnc5QXhhR2l4SXMyeVBraGRoZVYtdVVZVEt3SnFZcVZEU2wwQ0FsSGQtZUU");
        folder2.setMimeType("application/vnd.google-apps.folder");
        folder2.setCreatedDate(new DateTime("2015-01-07T15:14:10.751Z"));

        folder2.setParents(this.getParentReferenceList(
                "0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE", false
        ));

        folder2.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(folder2);

        File file2 = new File();
        file2.setTitle("file2");
        file2.setId("1dnzyHctDRyZVZztz7RX20i9u0TVnd7G-X5sP0BSU_QM");
        file2.setMimeType("application/vnd.google-apps.document");

        file2.setParents(this.getParentReferenceList(
                "0B3mMPOF_fWirfnc5QXhhR2l4SXMyeVBraGRoZVYtdVVZVEt3SnFZcVZEU2wwQ0FsSGQtZUU",
                false
        ));

        file2.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file2);

        TreeBuilder treeBuilder = new TreeBuilder(listFile);
        TreeNode root = treeBuilder.getRoot();

        assertEquals("0AHRgC7jH8BP_Uk9PVA", root.getId());
        assertEquals(1, root.getChildren().size());

        TreeNode expected1 = root.getChildren().get(0);

        assertEquals("folder1", expected1.getTitle());
        assertEquals(2, expected1.getChildren().size());

        TreeNode expected2Folder = expected1.getChildren().get(1);
        assertEquals("folder2", expected2Folder.getTitle());
        assertEquals("file2", expected2Folder.getChildren().get(0).getTitle());
    }

    @Test(timeout = 10000)
    public void testOwnerShip(){
        ArrayList<File>listFile = new ArrayList<>();

        File file1 = new File();
        file1.setTitle("file1");
        file1.setId("1K2T_qDWBhlyk_OVL9Q6JYmQZVcIo-Y9HSKz54RAMPhM");
        file1.setMimeType("application/vnd.google-apps.document");

        file1.setParents(this.getParentReferenceList(
                "0AHRgC7jH8BP_Uk9PVA",
                true
        ));

        file1.setOwners(this.getOwnerList("David Maignan", true));

        listFile.add(file1);


        File file2 = new File();
        file2.setTitle("file2");
        file2.setId("0B3mMPOF_fWirfk9xWW5iX09fWkdwR3I4dnV5cnV3Y1l4NDNkVUd6TzJfdGJHRVFSc2ctdkE");
        file2.setMimeType("application/vnd.google-apps.folder");

        file2.setParents(this.getParentReferenceList(
                "0AHRgC7jH8BP_Uk9PVA",
                true
        ));

        file2.setOwners(this.getOwnerList("David Maignan", false));

        listFile.add(file2);

        TreeBuilder treeBuilder = new TreeBuilder(listFile);
        TreeNode root = treeBuilder.getRoot();

        assertEquals(1, root.getChildren().size());
    }
}