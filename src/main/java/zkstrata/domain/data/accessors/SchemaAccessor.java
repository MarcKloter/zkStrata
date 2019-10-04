package zkstrata.domain.data.accessors;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.Schema;
import zkstrata.domain.data.types.Reference;
import zkstrata.domain.data.types.Value;

public class SchemaAccessor implements ValueAccessor {
    private String subject;
    private Schema schema;

    public SchemaAccessor(String subject, Schema schema) {
        this.subject = subject;
        this.schema = schema;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Value getValue(Selector selector) {
        return new Reference(schema.getType(selector), subject, selector);
    }
}
