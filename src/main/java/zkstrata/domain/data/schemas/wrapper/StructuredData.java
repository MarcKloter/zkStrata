package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.Variable;

public interface StructuredData<T extends Variable> {
    String getAlias();
    T getVariable(Selector selector);


    default Value resolve(Schema schema, Selector selector, ValueAccessor accessor) {
        Class<?> type = schema.getType(selector);

        Value value = accessor.getValue(selector);

        if (value == null) {
            String msg = String.format("Subject %s is missing the entry %s.", getAlias(), selector);
            throw new IllegalArgumentException(msg);
        }

        if (value.getType() != type) {
            String msg = String.format("Subject %s does not match schema %s.", getAlias(), schema.getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        return value;
    }
}
