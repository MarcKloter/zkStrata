package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Clause;
import zkstrata.utils.StatementBuilder;

public class And extends Connective {
    private Clause left;
    private Clause right;

    public And(Clause left, Clause right, Position position) {
        super(position);
        this.left = left;
        this.right = right;
    }

    @Override
    public Clause getLeft() {
        return left;
    }

    @Override
    public Clause getRight() {
        return right;
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        left.addTo(statementBuilder);
        right.addTo(statementBuilder);
    }
}
