package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;
import zkstrata.utils.StatementBuilder;

public abstract class Predicate extends AbstractTraceable {
    public Predicate(Position position) {
        super(position);
    }

    public abstract void addTo(StatementBuilder statementBuilder);
}
