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

    ValueAccessor getAccessor();

    T getVariable(Selector selector, Position.Absolute position);

    default Value resolve(Schema schema, Selector selector) {
        ValueAccessor accessor = getAccessor();

        Value value = accessor.getValue(selector);

        if (value == null) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: "
                    + "Missing entry `%s`.", getAlias(), schema.getSource(), selector);
            throw new IllegalArgumentException(msg);
        }

        Class<?> expectedType = schema.getType(selector);

        if (expectedType != value.getType()) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: "
                    + "Type mismatch. Found: %s, expected: %s.", getAlias(), schema.getClass().getSimpleName(),
                    value.getType().getSimpleName(), expectedType.getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        return value;
    }
}
