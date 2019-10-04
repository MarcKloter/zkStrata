package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Literal;

public class InstanceVariable extends AbstractVariable {
    public InstanceVariable(Literal value) {
        super(value);
    }

    @Override
    public Literal getValue() {
        return (Literal) super.getValue();
    }
}
