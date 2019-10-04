package zkstrata.domain.data.types.wrapper;

import zkstrata.domain.data.types.Value;
import zkstrata.exceptions.InternalCompilerErrorException;

public abstract class AbstractVariable implements Variable {
    private Value value;

    public AbstractVariable(Value value) {
        this.value = value;
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
    public String toString() {
        throw new InternalCompilerErrorException("Method toString is not allowed on objects of class Variable.");
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

        return value.equals(((Variable) obj).getValue());
    }
}
