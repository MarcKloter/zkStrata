package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

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

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        statementBuilder.equality(left.toString(), right.toString());
    }
}
