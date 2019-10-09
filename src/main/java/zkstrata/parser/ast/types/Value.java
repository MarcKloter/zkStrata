package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.Traceable;

public abstract class Value extends Traceable {
    public Value(Position position) {
        super(position);
    }
}
