package zkstrata.parser.ast.types;

import zkstrata.parser.ast.Position;

public abstract class Literal<T> extends Value {
    public Literal(Position position) {
        super(position);
    }

    public abstract T getValue();
}
