package zkstrata.parser.ast.predicates;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;
import zkstrata.parser.ast.Node;

public abstract class Predicate extends AbstractTraceable implements Node {
    public Predicate(Position position) {
        super(position);
    }
}
