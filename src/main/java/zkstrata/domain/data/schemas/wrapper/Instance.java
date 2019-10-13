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
public class Instance implements StructuredData<InstanceVariable> {
    private String alias;
    private Schema schema;
    private ValueAccessor accessor;

    public Instance(String alias, Schema schema, ValueAccessor accessor) {
        if (accessor == null) {
            String msg = String.format("Missing instance data for %s.", alias);
            throw new IllegalArgumentException(msg);
        }

        this.alias = alias;
        this.schema = schema;
        this.accessor = accessor;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public Schema getSchema() {
        return schema;
    }

    @Override
    public InstanceVariable getVariable(Selector selector, Position.Absolute position) {
        // TODO: test case (instance is subject -> referenced)
        // TODO: ensure missing instance is thrown before or check here
        Literal value = (Literal) resolve(schema, selector, accessor);
        return new InstanceVariable(value, position);
    }
}
