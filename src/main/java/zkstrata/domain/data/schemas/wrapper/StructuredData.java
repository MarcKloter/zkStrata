package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Metadata;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.Position;

public interface StructuredData<T extends Variable> {
    String getAlias();

    Schema getSchema();

    boolean isWitness();

    ValueAccessor getAccessor();

    ValueAccessor getMetaData();

    T getVariable(Selector selector, Position.Absolute position);

    default int indexOfMetadata(Schema schema, Selector selector) {
        for (int i = 1; i <= selector.getSelectors().size(); i++)
            if (Metadata.class.isAssignableFrom(schema.getType(new Selector(selector.getSelectors().subList(0, i)))))
                return i - 1;

        return -1;
    }

    default Value resolve(Schema schema, Selector selector) {
        ValueAccessor accessor = getAccessor();

        Class<?> type = schema.getType(selector);

        int index = indexOfMetadata(schema, selector);
        if (index > -1) {
            accessor = getMetaData();

            if (accessor == null) {
                String msg = String.format("Missing metadata for `%s`.", getAlias());
                throw new IllegalArgumentException(msg);
            }

            selector = new Selector(selector.getSelectors().subList(index + 1, selector.getSelectors().size()));
        }

        Value value = accessor.getValue(selector);

        if (value == null) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: "
                    + "Missing entry `%s`.", getAlias(), schema.getSource(), selector);
            throw new IllegalArgumentException(msg);
        }

        if (type != value.getType()) {
            String msg = String.format("The provided data for subject `%s` does not match the schema `%s`: Type mismatch.",
                    getAlias(), schema.getClass().getSimpleName());
            throw new IllegalArgumentException(msg);
        }

        return value;
    }
}
