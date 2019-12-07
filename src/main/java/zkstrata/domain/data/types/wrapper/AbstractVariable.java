package zkstrata.domain.data.types.wrapper;

import zkstrata.analysis.TypeConstraintsChecker;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.AbstractTraceable;

public abstract class AbstractVariable extends AbstractTraceable implements Variable {
    private Value value;
    private Reference reference;

    public AbstractVariable(Value value, Reference reference, Position.Absolute position) {
        super(position);
        this.value = value;
        this.reference = reference;

        if (Literal.class.isAssignableFrom(value.getClass()))
            TypeConstraintsChecker.process((Literal) value, position);
    }

    @Override
    public Reference getReference() {
        return reference;
    }

    @Override
    public Position.Absolute getPosition() {
        return (Position.Absolute) super.getPosition();
    }

    @Override
    public Class<?> getType() {
        return value.getType();
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return getValue().equals(((Variable) obj).getValue());
    }
}
