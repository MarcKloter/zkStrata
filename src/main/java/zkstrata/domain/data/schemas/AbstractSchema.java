package zkstrata.domain.data.schemas;

import zkstrata.domain.data.Selector;
import zkstrata.utils.ReflectionHelper;

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
            Field field = ReflectionHelper.getField(schema, accessor);
            Class<?> type = field.getType();

            if (!iterator.hasNext()) {
                return type;
            } else {
                ReflectionHelper.assertIsAssignableFrom(Schema.class, type);
                schema = type.asSubclass(Schema.class);
            }
        }

        throw new IllegalArgumentException("Accessors cannot be empty.");
    }

    @Override
    public boolean hasValidationRule() {
        return getValidationRule() != null;
    }

    @Override
    public String getValidationRule() {
        return null;
    }

    @Override
    public String getSource() {
        return getIdentifier();
    }
}
