package zkstrata.domain.data.schemas.dynamic;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.schemas.AbstractSchema;

public class JsonSchema extends AbstractSchema {
    private String filename;

    public JsonSchema(String filename) {
        this.filename = filename;
    }

    @Override
    public Class<?> getType(Selector selector) {
        // TODO: custom getter (obvious reason = dynamic)
        return super.getType(selector);
    }
}
