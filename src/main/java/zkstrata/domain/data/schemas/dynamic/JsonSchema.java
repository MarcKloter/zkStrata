package zkstrata.domain.data.schemas.dynamic;

import zkstrata.domain.data.Selector;
import zkstrata.domain.data.accessors.JsonAccessor;
import zkstrata.domain.data.schemas.AbstractSchema;
import zkstrata.domain.data.types.Value;
import zkstrata.domain.data.types.custom.HexLiteral;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.parser.ParseTreeVisitor;
import zkstrata.parser.ast.AbstractSyntaxTree;
import zkstrata.utils.StatementBuilder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JsonSchema extends AbstractSchema {
    private static final String PROPERTIES = "properties";
    private static final String TYPE = "type";
    private static final String MAXIMUM = "maximum";
    private static final String MINIMUM = "minimum";

    private String identifier;
    private JsonAccessor accessor;

    public JsonSchema(String filename, String identifier) {
        this.identifier = identifier;
        this.accessor = new JsonAccessor(filename);
    }

    @Override
    public Class<?> getType(Selector selector) {
        List<String> selectors = selector.getSelectors();
        String typeString = getTypeDefinition(selectors);

        try {
            Class<?> type = JSONType.valueOf(typeString.toUpperCase()).getType();

            if (type == String.class && selectors.get(selectors.size() - 1).endsWith("_hex"))
                return HexLiteral.class;

            return type;
        } catch (IllegalArgumentException e) {
            String msg = String.format("Unknown type `%s` for property `%s` in schema %s.",
                    typeString, selector, accessor.getSource());
            throw new IllegalArgumentException(msg);
        }
    }

    private String getTypeDefinition(List<String> selectors) {
        List<String> typeSelector = constructPropertySelector(selectors);
        typeSelector.add(TYPE);

        Value typeString = accessor.getValue(new Selector(typeSelector));
        if (typeString == null) {
            String msg = String.format("The provided schema %s is missing a type definition for property `%s`.",
                    accessor.getSource(), String.join(".", selectors));
            throw new IllegalArgumentException(msg);
        }

        if (typeString.getType() != String.class) {
            String msg = String.format("Invalid type for property `%s` in schema %s. "
                            + "Each instance must be restricted to exactly one primitive type.",
                    String.join(".", selectors), accessor.getSource());
            throw new IllegalArgumentException(msg);
        }

        return typeString.toString();
    }

    private List<String> constructPropertySelector(List<String> selectors) {
        List<String> propertySelector = new ArrayList<>();
        for (String selector : selectors) {
            propertySelector.add(PROPERTIES);
            propertySelector.add(selector);
        }
        return propertySelector;
    }

    @Override
    public boolean hasValidationRule() {
        return accessor.getValue(new Selector("validationRule")) != null;
    }

    @Override
    public String getValidationRule() {
        Value validationRule = accessor.getValue(new Selector("validationRule"));

        if (validationRule == null)
            return null;

        if (validationRule.getType() != String.class) {
            String msg = String.format("Invalid validation rule in schema %s. The statement must be a string.", accessor.getSource());
            throw new IllegalArgumentException(msg);
        }

        AbstractSyntaxTree ast = new ParseTreeVisitor().parse(accessor.getSource(), validationRule.toString(), "THIS");
        StatementBuilder statementBuilder = new StatementBuilder(ast);
        parseValidationKeywords(Collections.emptyList(), statementBuilder);

        return statementBuilder.build();
    }

    private void parseValidationKeywords(List<String> selectors, StatementBuilder statementBuilder) {
        List<String> propertiesSelector = constructPropertySelector(selectors);
        propertiesSelector.add(PROPERTIES);

        Set<String> properties = accessor.getKeySet(propertiesSelector);

        for (String property : properties) {
            List<String> propertySelector = new ArrayList<>(selectors);
            propertySelector.add(property);
            String type = getTypeDefinition(propertySelector);

            propertiesSelector.add(property);
            Set<String> validationKeywords = accessor.getKeySet(propertiesSelector);
            String witness = String.format("private.%s", String.join(".", propertySelector));

            switch (type) {
                case "object":
                    parseValidationKeywords(propertySelector, statementBuilder);
                    break;
                case "number":
                    if (validationKeywords.contains(MAXIMUM))
                        statementBuilder.boundsCheck(witness, null, getNumberConstraint(MAXIMUM, propertySelector));
                    if (validationKeywords.contains(MINIMUM))
                        statementBuilder.boundsCheck(witness, getNumberConstraint(MINIMUM, propertySelector), null);
                    break;
                case "string":
                    // TODO: implement string validation keywords
                    break;
                default:
                    break;
            }
        }
    }

    private String getNumberConstraint(String keyword, List<String> selectors) {
        List<String> assertionSelector = constructPropertySelector(selectors);
        assertionSelector.add(keyword);
        Object assertion = accessor.getObject(assertionSelector);

        if (!(assertion instanceof Integer))
            throw new IllegalArgumentException(String.format("Invalid value for validation keyword `%s` of `%s` in %s.",
                    keyword, String.join(".", selectors), accessor.getSource()));

        return String.valueOf(assertion);
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
        STRING(String.class),
        NUMBER(BigInteger.class),
        BOOLEAN(Boolean.class),
        NULL(Null.class);

        private Class<?> type;

        JSONType(Class<?> type) {
            this.type = type;
        }

        public Class<?> getType() {
            return type;
        }
    }
}
