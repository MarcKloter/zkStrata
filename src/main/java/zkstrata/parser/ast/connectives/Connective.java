package zkstrata.parser.ast.connectives;

import zkstrata.exceptions.AbstractTraceable;
import zkstrata.exceptions.Position;
import zkstrata.parser.ast.Clause;

public abstract class Connective extends AbstractTraceable implements Clause {
    public Connective(Position position) {
        super(position);
    }

    public abstract Clause getLeft();

    public abstract Clause getRight();
}
