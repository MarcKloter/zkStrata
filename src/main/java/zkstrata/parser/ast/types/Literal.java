package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;

public abstract class Literal<T> extends Value {
    public Literal(Position position) {
        super(position);
    }
}
