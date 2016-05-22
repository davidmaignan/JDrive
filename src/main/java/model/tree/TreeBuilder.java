package model.tree;

import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree File structure
 *
 * David Maignan <davidmaignan@gmail.com>
 */
public class TreeBuilder {
    private static Logger logger = LoggerFactory.getLogger(TreeBuilder.class.getSimpleName());
    private TreeNode root;

    public TreeBuilder(String rootId) {
        root = new TreeNode();
        root.setId(rootId);
    }

    /**
     * Build tree from a liste of files
     * @param list
     * @return
     */
    public TreeNode build(List<File> list) {
        List<TreeNode> nodeList = this.getNodeList(list);

        while (! nodeList.isEmpty()) {
            ArrayList<TreeNode> tmp = nodeList.stream()
                    .filter(node -> insertNode(root, node))
                    .collect(Collectors.toCollection(ArrayList::new));

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
        logger.debug(node.toString());

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

        if(node.getParentId() == null) {
            return true;
        }

        if (node.getParentId().equals(root.getId())) {
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
        return list.stream().map(TreeNode::new).collect(Collectors.toList());
    }
}
