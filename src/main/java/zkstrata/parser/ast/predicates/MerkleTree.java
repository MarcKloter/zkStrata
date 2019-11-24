package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.BinaryTree;
import zkstrata.utils.StatementBuilder;

public class MerkleTree extends Predicate {
    private Value root;
    private BinaryTree<Value> tree;

    public MerkleTree(Value root, BinaryTree<Value> tree, Position position) {
        super(position);
        this.root = root;
        this.tree = tree;
    }

    public Value getRoot() {
        return root;
    }

    public BinaryTree<Value> getTree() {
        return tree;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.merkleTree(root.toString(), visitBinaryTree(tree));
    }

    private BinaryTree<String> visitBinaryTree(BinaryTree binaryTree) {
        return new BinaryTree<>(visitBinaryTreeNode(binaryTree.getRoot()));
    }

    private BinaryTree.Node<String> visitBinaryTreeNode(BinaryTree.Node node) {
        if (!node.isLeaf()) {
            return new BinaryTree.Node<>(visitBinaryTreeNode(node.getLeft()), visitBinaryTreeNode(node.getRight()));
        } else {
            if (node.getValue() instanceof Value)
                return new BinaryTree.Node<>(node.getValue().toString());
            else
                throw new InternalCompilerException("Expected BinaryTree.Node of class Value, found %s.",
                        node.getValue().getClass().getSimpleName());
        }
    }
}
