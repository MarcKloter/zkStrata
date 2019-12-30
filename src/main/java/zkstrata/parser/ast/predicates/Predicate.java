package zkstrata.parser.ast.predicates;

import zkstrata.parser.ast.Node;

public abstract class Predicate implements Node {
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
