package zkstrata.domain.gadgets;

import zkstrata.domain.data.types.wrapper.Nullable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.optimizer.LocalOptimizationRule;

import java.lang.reflect.Field;
import java.util.*;

public abstract class AbstractGadget<T extends AbstractGadget> implements Gadget<T> {

    @Override
    public void initFrom(Map<String, Variable> sourceFields) {
        for (Map.Entry<String, Variable> source : sourceFields.entrySet()) {
            try {
                Field destination = this.getClass().getDeclaredField(source.getKey());
                Variable destinationValue = source.getValue();
                checkType(destination, destinationValue);
                if (destinationValue.getClass() == Nullable.class)
                    destinationValue = null;
                destination.setAccessible(true);
                destination.set(this, destinationValue);
            } catch (NoSuchFieldException e) {
                throw new InternalCompilerException("Missing field %s in gadget %s.", source.getKey(), this.getClass());
            } catch (IllegalAccessException e) {
                throw new InternalCompilerException("Unable to access field %s.", source.getKey());
            }
        }

        this.performChecks();
    }

    private void checkType(Field field, Variable variable) {
        Type annotation = field.getAnnotation(Type.class);

        if (annotation == null)
            throw new InternalCompilerException("Field %s in %s is missing @Type annotation.",
                    field.getName(), this.getClass());

        List<Class<?>> allowedTypes = Arrays.asList(annotation.value());

        if (!allowedTypes.contains(variable.getType()))
            throw new InternalCompilerException("Type %s not allowed for field %s in %s.",
                    variable.getType().getSimpleName(), field.getName(), this.getClass());
    }

    @Override
    public List<Variable> getVariables() {
        List<Variable> variables = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            Type annotation = field.getAnnotation(Type.class);

            if (annotation != null) {
                try {
                    field.setAccessible(true);
                    variables.add((Variable) field.get(this));
                } catch (IllegalAccessException e) {
                    throw new InternalCompilerException("Unable to access field %s in %s.",
                            field.getName(), this.getClass());
                }
            }
        }
        return variables;
    }

    /**
     * Empty default implementation to override by gadgets on demand.
     */
    @Override
    public void performChecks() {
    }

    @LocalOptimizationRule(context = {AbstractGadget.class})
    public Optional<Gadget> removeDuplicate(T other) {
        return isEqualTo(other) ? Optional.empty() : Optional.of(this);
    }
}
