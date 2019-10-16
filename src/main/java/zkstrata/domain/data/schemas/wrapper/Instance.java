package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.exceptions.Position;

/**
 * Wrapper class to mark a {@link Schema} as instance wide (to all participants, usually publicly) known.
 */
public class Instance extends AbstractStructuredData<InstanceVariable> {
    public Instance(String alias, Schema schema, ValueAccessor accessor) {
        super(alias, schema, accessor);

        if (accessor == null) {
            String msg = String.format("Missing instance data for %s.", alias);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public InstanceVariable getVariable(Selector selector, Position.Absolute position) {
        // TODO: test case (instance is subject -> referenced)
        // TODO: ensure missing instance is thrown before or check here
        Literal value = (Literal) resolve(getSchema(), selector, getAccessor());
        return new InstanceVariable(value, position);
    }
}
