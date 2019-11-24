package zkstrata.parser.ast.types;

import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public abstract class Value<T> extends AbstractTraceable {
    public abstract T getValue();

    public Value(Position position) {
        super(position);
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
