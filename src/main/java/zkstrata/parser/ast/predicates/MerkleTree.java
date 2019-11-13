package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.BinaryTree;

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
}
