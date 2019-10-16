package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public abstract class Value extends AbstractTraceable {
    public Value(Position position) {
        super(position);
    }
}
