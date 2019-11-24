package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.types.Value;
import zkstrata.utils.StatementBuilder;

public class Inequality extends Predicate {
    private Value left;
    private Value right;

    public Inequality(Value left, Value right, Position position) {
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
        statementBuilder.inequality(left.toString(), right.toString());
    }
}
