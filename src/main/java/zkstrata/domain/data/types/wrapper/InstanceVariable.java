package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Literal;
import zkstrata.exceptions.Position;

public class InstanceVariable extends AbstractVariable {
    public InstanceVariable(Literal value, Position.Absolute position) {
        super(value, position);
    }

    public static InstanceVariable of(Object value) {
        return new InstanceVariable(new Literal(value), null);
    }

    @Override
    public Literal getValue() {
        return (Literal) super.getValue();
    }
}
