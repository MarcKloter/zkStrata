package zkstrata.parser.ast.types;

import zkstrata.parser.ast.Leaf;
import zkstrata.parser.ast.Position;

public abstract class Value extends Leaf {
    public Value(Position position) {
        super(position);
    }
}
