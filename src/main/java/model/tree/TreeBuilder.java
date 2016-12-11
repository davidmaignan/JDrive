package model.tree;

import com.google.api.services.drive.model.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree File monitor
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

    public List<TreeNode> getNodes(){
        return getNodes(this.getRoot());
    }

    private List<TreeNode> getNodes(TreeNode node){
        List<TreeNode> result = new ArrayList<>();
        result.add(node);

        if(! node.getChildren().isEmpty()) {
            for(TreeNode child : node.getChildren()){
                result.addAll(getNodes(child));
            }
        }

        return result;
    }

    /**
     * Insert node recursively in a tree
     * @param root
     * @param node
     * @return boolean
     */
    public boolean insertNode(TreeNode root, TreeNode node) {
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
     * Insert node recursively in a tree
     * @param node
     * @return boolean
     */
    public boolean insertNode(TreeNode node) {
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
