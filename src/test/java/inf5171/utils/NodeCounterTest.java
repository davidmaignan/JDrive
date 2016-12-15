package inf5171.utils;

import model.tree.TreeNode;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by david on 2016-12-14.
 */
public class NodeCounterTest {
    private NodeCounter nodeCounter;
    private TreeNode root;
    private Queue<TreeNode> childrenRoot;
    private Queue<TreeNode> childrenNode_3;
    private Queue<TreeNode> emptyChildren;

    @Before
    public void setUp() throws Exception {
        root = mock(TreeNode.class);
        childrenRoot = new ArrayDeque<>();
        childrenNode_3 = new ArrayDeque<>();
        emptyChildren = new ArrayDeque<>();
        nodeCounter = new NodeCounter(root);
    }

    @Test
    public void computeEmptyTree() throws Exception {
        when(root.getChildren()).thenReturn(emptyChildren);
        assertEquals(1, (int)nodeCounter.compute());
    }

    @Test
    public void computeTree() throws Exception {
        TreeNode child_1 = mock(TreeNode.class);
        TreeNode child_2 = mock(TreeNode.class);
        TreeNode child_3 = mock(TreeNode.class);

        TreeNode child_33 = mock(TreeNode.class);

        childrenRoot.add(child_1);
        childrenRoot.add(child_2);
        childrenRoot.add(child_3);

        childrenNode_3.add(child_33);

        when(child_1.getChildren()).thenReturn(emptyChildren);
        when(child_2.getChildren()).thenReturn(emptyChildren);
        when(child_3.getChildren()).thenReturn(childrenNode_3);
        when(child_33.getChildren()).thenReturn(emptyChildren);

        when(root.getChildren()).thenReturn(childrenRoot);

        assertEquals(5, (int)nodeCounter.compute());
    }

    @Test
    public void countNodes() throws Exception {
        TreeNode child_1 = mock(TreeNode.class);
        TreeNode child_2 = mock(TreeNode.class);
        TreeNode child_3 = mock(TreeNode.class);

        TreeNode child_33 = mock(TreeNode.class);

        childrenRoot.add(child_1);
        childrenRoot.add(child_2);
        childrenRoot.add(child_3);

        childrenNode_3.add(child_33);

        when(child_1.getChildren()).thenReturn(emptyChildren);
        when(child_2.getChildren()).thenReturn(emptyChildren);
        when(child_3.getChildren()).thenReturn(childrenNode_3);
        when(child_33.getChildren()).thenReturn(emptyChildren);

        when(root.getChildren()).thenReturn(childrenRoot);

        assertEquals(5, (int)NodeCounter.countNodes(root));
    }

}