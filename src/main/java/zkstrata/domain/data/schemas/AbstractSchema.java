package zkstrata.domain.data.schemas;

import zkstrata.domain.data.Selector;

import java.lang.reflect.Field;
import java.util.Iterator;

public abstract class AbstractSchema implements Schema {
    /**
     * Returns the class of the field referenced by the given selectors.
     *
     * @param selector selector as reference to the target field
     * @return class object that represents the type of the referenced field
     */
    @Override
    public Class<?> getType(Selector selector) {
        Class<? extends Schema> schema = this.getClass();
        Iterator<String> iterator = selector.getSelectors().iterator();
        while (iterator.hasNext()) {
            String accessor = iterator.next();
            Field field = getField(schema, accessor);
            Class<?> type = field.getType();

            if (!iterator.hasNext()) {
                return type;
            } else {
                if (Schema.class.isAssignableFrom(type)) {
                    schema = type.asSubclass(Schema.class);
                } else {
                    // TODO: add test case for this
                    String msg = String.format("Cannot access %s because it is not a schema.", accessor);
                    throw new IllegalArgumentException(msg);
                }
            }
        }

        throw new IllegalArgumentException("Accessors cannot be empty.");
    }

    private Field getField(Class<? extends Schema> schema, String selector) {
        try {
            return schema.getDeclaredField(selector);
        } catch (NoSuchFieldException e) {
            String msg = String.format("Field %s does not exist in schema %s.", selector, schema);
            throw new IllegalArgumentException(msg);
        }
    }

    @Override
    public String getStatement() {
        return null;
    }

    @Override
    public String getSource() {
        return getIdentifier();
    }
}
