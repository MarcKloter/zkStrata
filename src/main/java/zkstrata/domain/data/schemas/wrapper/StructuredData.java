package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.Position;

public interface StructuredData<T extends Variable> {
    String getAlias();

    Schema getSchema();

    boolean isWitness();

    T getVariable(Selector selector, Position.Absolute position);

    default Value resolve(Schema schema, Selector selector, ValueAccessor accessor) {
        Value value = accessor.getValue(selector);

        if (value == null) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: "
                    + "Missing entry `%s`.", getAlias(), schema.getSource(), selector);
            throw new IllegalArgumentException(msg);
        }

        Class<?> type = schema.getType(selector);

        if (value.getType() != type) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: Type mismatch.",
                    getAlias(), schema.getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        return value;
    }
}
