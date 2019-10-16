package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

// TODO: do we utilize position information for these predicate objects?
public abstract class Predicate extends AbstractTraceable {
    public Predicate(Position position) {
        super(position);
    }
}
