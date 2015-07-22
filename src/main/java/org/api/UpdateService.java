package org.api;

import com.google.api.services.drive.Drive;
import com.google.inject.Inject;
import org.model.tree.TreeNode;

/**
 * Update service
 *
 * Update file locally after recieving changes notification from Drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class UpdateService {
    private final FileService fileService;
    private TreeNode node;
    private Drive.Changes changeList;

    @Inject
    public UpdateService(FileService fileService) {
        this.fileService = fileService;
    }

    public void setNode(TreeNode node) {
        this.node = node;
    }

    public void update(Drive.Changes changeList){
        this.changeList = changeList;

//        for(Change change : changeList) {
//
//        }

    }

}
