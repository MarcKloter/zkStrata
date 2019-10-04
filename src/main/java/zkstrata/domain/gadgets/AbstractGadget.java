package zkstrata.domain.gadgets;

import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.optimizer.LocalOptimizationRule;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractGadget<T extends AbstractGadget> implements Gadget<T> {

    @Override
    public void initFrom(Map<String, Variable> sourceFields) {
        for (Map.Entry<String, Variable> source : sourceFields.entrySet()) {
            try {
                Field destination = this.getClass().getDeclaredField(source.getKey());
                Variable destinationValue = source.getValue();
                checkType(destination, destinationValue);
                destination.setAccessible(true);
                destination.set(this, destinationValue);
            } catch (NoSuchFieldException e) {
                String msg = String.format("Missing field %s in gadget %s.",
                        source.getKey(), this.getClass());
                throw new IllegalStateException(msg);
            } catch (IllegalAccessException e) {
                String msg = String.format("Unable to access field %s.", source.getKey());
                throw new IllegalStateException(msg);
            }
        }

        this.onInit();
    }

    private void checkType(Field field, Variable variable) {
        Type annotation = field.getAnnotation(Type.class);

        if (annotation == null) {
            String msg = String.format("Field %s in %s is missing @Type annotation.",
                    field.getName(), this.getClass());
            throw new IllegalStateException(msg);
        }

        List<Class<?>> allowedTypes = Arrays.asList(annotation.value());

        if (!allowedTypes.contains(variable.getType())) {
            String msg = String.format("Type %s not allowed for field %s in %s.",
                    variable.getType().getSimpleName(), field.getName(), this.getClass());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * Empty default implementation to override by gadgets on demand.
     */
    @Override
    public void onInit() {
    }

    @LocalOptimizationRule(context = {AbstractGadget.class})
    public Optional<Gadget> checkEquality(T other) {
        return isEqualTo(other) ? Optional.empty() : Optional.of(this);
    }
}
