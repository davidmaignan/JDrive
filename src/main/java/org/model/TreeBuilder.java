package org.model;

import com.google.api.services.drive.model.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TreeBuilder: construct a Tree representing the
 * Drive file directory structure
 *
 * Created by David Maignan <davidmaignan@gmail.com> on 15-07-15.
 */
public class TreeBuilder {

    private List<File> fileList;

    private TreeNode root;

    public TreeBuilder(String rootFolder, List<File> list) {
        root = new TreeNode(rootFolder);

        this.fileList           = list;
        List<TreeNode> nodeList = this.getNodeList(list);
        boolean exit            = false;
        int total               = list.size();

        while (! nodeList.isEmpty() && ! exit) {
            ArrayList<TreeNode> tmp = new ArrayList<>();

            for (TreeNode node : nodeList) {
                if (insertNode(root, node)) {
                    tmp.add(node);
                }
            }

            if (tmp.size() == 0)
                exit = true;

            nodeList.removeAll(tmp);

            //No file is at the root level (for the first loop)
            if (total == list.size()) {
                exit = true;
            }
        }
    }

    private List<TreeNode> getNodeList(List<File> list){
        List<TreeNode> nodeList = new ArrayList<>();

        for (File file : fileList) {
            nodeList.add(new TreeNode(file));
        }

        return nodeList;
    }

    /**
     * Insert node recursively in a tree
     * @param root
     * @param node
     * @return boolean
     */
    private boolean insertNode(TreeNode root, TreeNode node) {
        Boolean result = false;

        if (! node.isAuthenticatedUser()) {
            //@todo shared with me files/folder
            result = true;

        } else if (node.isRoot()) {
            root.addChild(node);
            root.setId(node.getParentId());

            result = true;
            
        } else if (node.getParentId() != null
                && root.getId() != null
                && root.getId().equals(node.getParentId())){
            root.addChild(node);

            result = true;

        } else if (root.getChildren().size() > 0) {
            for(TreeNode n : root.getChildren()) {
                result = insertNode(n, node) || result;
            }
        }

        return result;
    }

    /**
     * Get root for the tree
     *
     * @return TreeNode
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * Debug code
     * @param node
     */
    public static void printTree(TreeNode node) {
        System.out.println(node);

        List<TreeNode> children = node.getChildren();

        if(! children.isEmpty()) {
            for(TreeNode child : children){
                printTree(child);
            }
        }
    }
}
