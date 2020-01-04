package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.exceptions.Position;

public class InstanceVariable extends AbstractVariable {
    public InstanceVariable(Literal value, Reference reference, Position.Absolute position) {
        super(value, reference, position);
    }

    public static InstanceVariable of(Object value) {
        return new InstanceVariable(new Literal(value), null, null);
    }

    @Override
    public Literal getValue() {
        return (Literal) super.getValue();
    }
}
