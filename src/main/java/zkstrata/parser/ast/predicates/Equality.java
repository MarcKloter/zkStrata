package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;

public class Equality extends Predicate {
    private Value left;
    private Value right;

    public Equality(Value left, Value right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    public Value getLeft() {
        return left;
    }

    public Value getRight() {
        return right;
    }
}
