package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.Selector;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.Position;

/**
 * wrapper class to mark a {@link Schema} as witness (secret knowledge)
 */
public class Witness implements StructuredData<WitnessVariable> {
    private String alias;
    private Schema schema;
    private ValueAccessor accessor;

    public Witness(String alias, Schema schema, ValueAccessor accessor) {
        this.alias = alias;
        this.schema = schema;
        this.accessor = accessor;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    @Override
    public WitnessVariable getVariable(Selector selector, Position position) {
        try {
            Value value = resolve(schema, selector, accessor);
            return new WitnessVariable(value, selector, position);
        } catch (ClassCastException e) {
            String msg = String.format("Instance data %s does not match the structure of schema %s.", alias, schema.getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }
    }
}
