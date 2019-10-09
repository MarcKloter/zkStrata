package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;

// TODO: do we utilize position information for these predicate objects?
public abstract class Predicate extends Traceable {
    public Predicate(Position position) {
        super(position);
    }
}
