package zkstrata.domain.data.schemas.wrapper;

import zkstrata.domain.data.accessors.ValueAccessor;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.wrapper.Variable;

public abstract class AbstractStructuredData<T extends Variable> implements StructuredData<T> {
    private String alias;
    private Schema schema;
    private ValueAccessor accessor;
    private ValueAccessor metaData;

    AbstractStructuredData(String alias, Schema schema, ValueAccessor accessor, ValueAccessor metaData) {
        this.alias = alias;
        this.schema = schema;
        this.accessor = accessor;
        this.metaData = metaData;
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
    public ValueAccessor getAccessor() {
        return accessor;
    }

    public ValueAccessor getMetaData() {
        return metaData;
    }
}
