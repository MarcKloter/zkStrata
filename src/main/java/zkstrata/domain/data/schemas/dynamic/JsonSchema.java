package zkstrata.domain.data.schemas.dynamic;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.Null;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class JsonSchema extends AbstractSchema {
    private String identifier;
    private JsonAccessor accessor;

    public JsonSchema(String filename, String identifier) {
        this.identifier = identifier;
        this.accessor = new JsonAccessor(filename);
    }

    @Override
    public Class<?> getType(Selector selector) {
        List<String> selectors = selector.getSelectors();
        List<String> typeSelector = new ArrayList<>();
        selectors.forEach(curr -> {
            typeSelector.add("properties");
            typeSelector.add(curr);
        });
        typeSelector.add("type");

        Value typeString = accessor.getValue(new Selector(typeSelector));
        if (typeString == null) {
            String msg = String.format("The provided schema %s is missing a type definition for property `%s`.",
                    accessor.getSource(), selector);
            throw new IllegalArgumentException(msg);
        }

        if (typeString.getType() != String.class) {
            String msg = String.format("Invalid type for property `%s` in schema %s. "
                            + "Each instance must be restricted to exactly one primitive type.",
                    String.join(".", selectors), accessor.getSource());
            throw new IllegalArgumentException(msg);
        }

        try {
            Class<?> type = JSONType.valueOf(typeString.toString().toUpperCase()).getType();

            if (type == String.class && selectors.get(selectors.size() - 1).endsWith("_hex"))
                return HexLiteral.class;

            return type;
        } catch (IllegalArgumentException e) {
            String msg = String.format("Unknown type `%s` for property `%s` in schema %s.",
                    typeString.toString(), String.join(".", selectors), accessor.getSource());
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String getValidationRule() {
        Value statement = accessor.getValue(new Selector(List.of("validationRule")));

        if (statement == null)
            return null;

        if (statement.getType() != String.class) {
            String msg = String.format("Invalid statement in schema %s. The statement must be a string.", accessor.getSource());
            throw new IllegalArgumentException(msg);
        }

        return statement.toString();
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getSource() {
        return accessor.getSource();
    }

    /**
     * Primitive types in JSON as of RFC 8259
     */
    private enum JSONType {
        STRING("string", String.class),
        NUMBER("number", BigInteger.class),
        BOOLEAN("boolean", Boolean.class),
        NULL("null", Null.class);

        private Class<?> type;

        JSONType(String name, Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
