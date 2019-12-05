package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Clause;
import zkstrata.utils.StatementBuilder;

public class Or extends Connective {
    public Or(Clause left, Clause right, Position position) {
        super(left, right, position);
    }

    @Override
    public void addTo(StatementBuilder statementBuilder) {
        StatementBuilder orBuilder = StatementBuilder.or();
        getLeft().addTo(orBuilder);
        getRight().addTo(orBuilder);
        statementBuilder.conjunction(orBuilder.build());
    }
}
