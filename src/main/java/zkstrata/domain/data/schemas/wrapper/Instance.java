package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as instance wide (to all participants, usually publicly) known.
 */
public class Instance extends AbstractStructuredData<InstanceVariable> {
    public Instance(String alias, Schema schema, ValueAccessor accessor) {
        super(alias, schema, accessor);
    }

    @Override
    public boolean isWitness() {
        return false;
    }

    @Override
    public InstanceVariable getVariable(Selector selector, Position.Absolute position) {
        if (getAccessor() == null)
            throw new CompileTimeException(String.format("Missing instance data for `%s`.", getAlias()), position);

        Literal value = (Literal) resolve(getSchema(), selector);
        return new InstanceVariable(value, new Reference(value.getType(), getAlias(), selector), position);
    }
}
