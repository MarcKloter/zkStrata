package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;
import zkstrata.parser.ast.Clause;

public abstract class Predicate extends AbstractTraceable implements Clause {
    public Predicate(Position position) {
        super(position);
    }
}
