package utils;

import org.junit.jupiter.api.Test;
import zkstrata.utils.BinaryTree;

import static org.junit.jupiter.api.Assertions.*;

public class BinaryTreeTest {
    private static final BinaryTree.Node<String> LEAF_1 = new BinaryTree.Node<>("One");
    private static final BinaryTree.Node<String> LEAF_2 = new BinaryTree.Node<>("Two");
    private static final BinaryTree.Node<String> LEAF_3 = new BinaryTree.Node<>("Three");
    private static final BinaryTree.Node<String> LEAF_4 = new BinaryTree.Node<>("Four");

    private static final BinaryTree<String> TREE_1A = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<String> TREE_1B = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<String> TREE_2A = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<String> TREE_2B = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, new BinaryTree.Node<>(LEAF_3, LEAF_2)));
    private static final BinaryTree<String> TREE_3A = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(LEAF_1, LEAF_4), LEAF_2));
    private static final BinaryTree<String> TREE_3B = new BinaryTree<>(new BinaryTree.Node<>(new BinaryTree.Node<>(null, LEAF_4), LEAF_2));
    private static final BinaryTree<String> TREE_4A = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, LEAF_4));
    private static final BinaryTree<String> TREE_4B = new BinaryTree<>(new BinaryTree.Node<>(LEAF_1, null));

    @Test
    void Equals_Null() {
        assertNotEquals(TREE_1A, null);
        assertNotEquals(TREE_2A, null);
    }

    @Test
    void Equals_Other_Object() {
        assertNotEquals(TREE_1A, "String");
        assertNotEquals(TREE_2A, 1234);
    }

    @Test
    void Equals() {
        assertEquals(TREE_1A, TREE_1B);
        assertEquals(TREE_2A, TREE_2B);
    }

    @Test
    void Not_Equals() {
        assertNotEquals(TREE_3A, TREE_3B);
        assertNotEquals(TREE_4A, TREE_4B);
    }

    @Test
    void HashCode() {
        assertEquals(TREE_1A.hashCode(), TREE_1B.hashCode());
        assertEquals(TREE_2A.hashCode(), TREE_2B.hashCode());
    }
}
