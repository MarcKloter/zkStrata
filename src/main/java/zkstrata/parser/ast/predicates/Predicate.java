package zkstrata.parser.ast.predicates;

import zkstrata.parser.ast.Leaf;
import zkstrata.parser.ast.Position;

// TODO: do we utilize position information for these predicate objects?
public abstract class Predicate extends Leaf {
    public Predicate(Position position) {
        super(position);
    }
}
