package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.AbstractTraceable;
import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Node;

public abstract class Connective extends AbstractTraceable implements Node {
    private Node left;
    private Node right;

    public Connective(Node left, Node right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName().toUpperCase();
    }
}
