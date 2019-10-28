package zkstrata.domain.data.schemas.dynamic;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.wrapper.Nullable;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class JsonSchema extends AbstractSchema {
    private String filename;
    private JsonAccessor accessor;

    public JsonSchema(String filename) {
        this.filename = filename;
        this.accessor = new JsonAccessor(filename);
    }

    @Override
    public Class<?> getType(Selector selector) {
        List<String> typeSelector = new ArrayList<>();
        selector.getSelectors().forEach(curr -> {
            typeSelector.add("properties");
            typeSelector.add(curr);
        });
        typeSelector.add("type");

        Value typeString = accessor.getValue(new Selector(typeSelector));
        if (typeString == null) {
            String msg = String.format("The provided schema %s is missing the type definition `%s`.",
                    filename, String.join(".", typeSelector));
            throw new IllegalArgumentException(msg);
        }

        if (typeString.getType() != String.class) {
            String msg = String.format("Invalid type for property `%s` in schema %s. "
                            + "Each instance must be restricted to exactly one primitive type.",
                    String.join(".", selector.getSelectors()), filename);
            throw new IllegalArgumentException(msg);
        }

        try {
            return JSONType.valueOf(typeString.toString().toUpperCase()).getType();
        } catch (IllegalArgumentException e) {
            String msg = String.format("Unknown type `%s` for property `%s` in schema %s.",
                    typeString.toString(), String.join(".", selector.getSelectors()), filename);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String getStatement() {
        Value statement = accessor.getValue(new Selector(List.of("statement")));

        if (statement == null)
            return null;

        if (statement.getType() != String.class) {
            String msg = String.format("Invalid statement in schema %s. The statement must be a string.", filename);
            throw new IllegalArgumentException(msg);
        }

        return statement.toString();
    }

    @Override
    public String getIdentifier() {
        return filename;
    }

    /**
     * Primitive types in JSON as of RFC 8259
     */
    private enum JSONType {
        STRING("string", String.class),
        NUMBER("number", BigInteger.class),
        BOOLEAN("boolean", Boolean.class),
        NULL("null", Nullable.class);

        private Class<?> type;

        JSONType(String name, Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
