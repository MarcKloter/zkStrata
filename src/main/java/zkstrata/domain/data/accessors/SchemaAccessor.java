package zkstrata.domain.data.accessors;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.Reference;

public class SchemaAccessor implements ValueAccessor {
    private String subject;
    private Schema schema;

    public SchemaAccessor(String subject, Schema schema) {
        this.subject = subject;
        this.schema = schema;
    }

    @Override
    public Reference getValue(Selector selector) {
        return new Reference(schema.getType(selector), subject, selector);
    }

    @Override
    public String getSource() {
        return schema.getIdentifier();
    }
}
