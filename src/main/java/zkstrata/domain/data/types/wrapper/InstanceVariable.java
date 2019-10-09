package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Literal;
import zkstrata.exceptions.Position;

public class InstanceVariable extends AbstractVariable {
    public InstanceVariable(Literal value, Position position) {
        super(value, position);
    }

    @Override
    public Literal getValue() {
        return (Literal) super.getValue();
    }
}
