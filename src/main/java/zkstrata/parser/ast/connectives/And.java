package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Node;
import zkstrata.utils.StatementBuilder;

public class And extends Connective {
    public And(Node left, Node right, Position position) {
        super(left, right, position);
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        getLeft().addTo(statementBuilder);
        getRight().addTo(statementBuilder);
    }
}
