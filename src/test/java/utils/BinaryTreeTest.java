package utils;

import org.junit.jupiter.api.Test;
import zkstrata.utils.BinaryTree;

import static org.junit.jupiter.api.Assertions.*;
import static zkstrata.utils.BinaryTree.Node;

public class BinaryTreeTest {
    private static final BinaryTree.Node<String> LEAF_1 = new BinaryTree.Node<>("One");
    private static final BinaryTree.Node<String> LEAF_2 = new BinaryTree.Node<>("Two");
    private static final BinaryTree.Node<String> LEAF_3 = new BinaryTree.Node<>("Three");
    private static final BinaryTree.Node<String> LEAF_4 = new BinaryTree.Node<>("Four");

    @Test
    void Not_Equals_Null() {
        BinaryTree<String> tree = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        assertNotEquals(null, tree);
    }

    @Test
    void Not_Equals_Other_Object() {
        BinaryTree<String> tree = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        assertNotEquals("String", tree);
        assertNotEquals(1234, tree);
    }

    @Test
    void Equals_1() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        assertEquals(treeA, treeB);
    }

    @Test
    void Equals_2() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(LEAF_1, new Node<>(LEAF_3, LEAF_2)));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(LEAF_1, new Node<>(LEAF_3, LEAF_2)));
        assertEquals(treeA, treeB);
    }

    @Test
    void Not_Equals_1() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), LEAF_2));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(new Node<>(null, LEAF_4), LEAF_2));
        assertNotEquals(treeA, treeB);
    }

    @Test
    void Not_Equals_2() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(LEAF_1, LEAF_4));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(LEAF_1, null));
        assertNotEquals(treeA, treeB);
    }

    @Test
    void HashCode_1() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        assertEquals(treeA.hashCode(), treeB.hashCode());
    }

    @Test
    void HashCode_2() {
        BinaryTree<String> treeA = new BinaryTree<>(new Node<>(LEAF_1, new Node<>(LEAF_3, LEAF_2)));
        BinaryTree<String> treeB = new BinaryTree<>(new Node<>(LEAF_1, new Node<>(LEAF_3, LEAF_2)));
        assertEquals(treeA.hashCode(), treeB.hashCode());
    }

    @Test
    void Count_Leaves_1() {
        BinaryTree<String> tree = new BinaryTree<>(new Node<>(new Node<>(LEAF_1, LEAF_4), new Node<>(LEAF_3, LEAF_2)));
        assertEquals(4, tree.getRoot().countLeaves());
    }

    @Test
    void Count_Leaves_2() {
        BinaryTree<String> tree = new BinaryTree<>(new Node<>(LEAF_1, null));
        assertEquals(1, tree.getRoot().countLeaves());
    }
}
