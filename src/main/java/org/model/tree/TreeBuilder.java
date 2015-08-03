package org.model.tree;

import com.google.api.services.drive.model.File;
import com.google.inject.Inject;
import org.config.Reader;

import java.util.ArrayList;
import java.util.List;

/**
 * Tree File structure of the Google Drive
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeBuilder {
    private List<File> fileList;

    private TreeNode root;

    private final Reader configReader;

    @Inject
    public TreeBuilder(Reader configReader) throws Exception {
        this.configReader = configReader;
        root              = new TreeNode(this.configReader.getProperty("rootFolder"));
    }

    /**
     * Build tree from a liste of files
     * @param list
     * @return
     */
    public TreeNode build(List<File> list) {
        this.fileList           = list;
        List<TreeNode> nodeList = this.getNodeList(list);

        while (! nodeList.isEmpty()) {
            ArrayList<TreeNode> tmp = new ArrayList<>();
            System.out.println(list.size());

            for (TreeNode node : nodeList) {
                if (insertNode(root, node)) {
                    tmp.add(node);
                }
            }

            nodeList.removeAll(tmp);
        }

        return root;
    }

    /**
     * Get root node
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
     * Get node list
     * @param list
     *
     * @return List<TreeNode>
     */
    private List<TreeNode> getNodeList(List<File> list) {
        List<TreeNode> nodeList = new ArrayList<>();

        for (File file : fileList) {
            nodeList.add(new TreeNode(file));
        }

        return nodeList;
    }
}
