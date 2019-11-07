package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public abstract class Predicate extends AbstractTraceable {
    public Predicate(Position position) {
        super(position);
    }
}
