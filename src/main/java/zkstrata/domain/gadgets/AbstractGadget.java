package zkstrata.domain.gadgets;

import zkstrata.domain.Proposition;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.optimizer.Substitution;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGadget implements Gadget {
    @Substitution(target = {Gadget.class, Gadget.class})
    public static Optional<Proposition> removeDuplicateGadget(Gadget first, Gadget second) {
        return first.equals(second) ? Optional.of(first) : Optional.empty();
    }

    @Substitution(target = {Gadget.class}, context = {Gadget.class})
    public static Optional<Proposition> removeConclusion(Gadget target, Gadget context) {
        return target.equals(context) ? Optional.of(Proposition.trueProposition()) : Optional.empty();
    }

    @Override
    public void initFrom(Map<String, Object> sourceFields) {
        for (Map.Entry<String, Object> source : sourceFields.entrySet()) {
            try {
                Field destination = ReflectionHelper.getField(this.getClass(), source.getKey());
                Object destinationValue = source.getValue();
                checkType(destination, destinationValue);
                if (destinationValue.getClass() == Null.class)
                    destinationValue = null;
                destination.setAccessible(true);
                destination.set(this, destinationValue);
            } catch (IllegalAccessException e) {
                throw new InternalCompilerException("Unable to access field %s.", source.getKey());
            }
        }

        this.performChecks();
    }

    /**
     * Checks whether the given {@link Field} accepts the provided object as value.
     *
     * @param field {@link Field} to check
     * @param value {@link Object} to type check against
     */
    private void checkType(Field field, Object value) {
        if (value instanceof Variable)
            checkTypeAnnotation(field, (Variable) value);
        else
            ReflectionHelper.assertIsAssignableFrom(field.getType(), value.getClass());
    }

    /**
     * Checks whether the given {@link Field}, which must be annotated as {@link Type}, accepts the type of the provided
     * {@link Variable}.
     *
     * @param field    {@link Field} to check the annotation for
     * @param variable {@link Variable} to type check against
     */
    private void checkTypeAnnotation(Field field, Variable variable) {
        Type annotation = field.getAnnotation(Type.class);

        if (annotation == null)
            throw new InternalCompilerException("Field %s in %s is missing @Type annotation.",
                    field.getName(), this.getClass());

        List<Class<?>> allowedTypes = Arrays.asList(annotation.value());

        // check whether the type of the variable is allowed for the gadget
        if (!allowedTypes.contains(Any.class) && !allowedTypes.contains(variable.getType()))
            throw new CompileTimeException(String.format("Unexpected type %s. Expected: %s.", variable.getType().getSimpleName(),
                    allowedTypes.stream().map(Class::getSimpleName).collect(Collectors.joining(", "))), variable);

        // check whether the variables confidentiality level (instance or witness) matches the gadgets requirements
        if (variable.getClass() != Null.class && !field.getType().isAssignableFrom(variable.getClass()))
            throw new CompileTimeException(String.format("%s not allowed here. Expected: %s.",
                    variable.getClass().getSimpleName(), field.getType().getSimpleName()), variable);
    }

    @Override
    public Map<String, Variable> getVariables() {
        Map<String, Variable> variables = new LinkedHashMap<>();

        for (Field field : getTypeAnnotatedFields()) {
            Object value = ReflectionHelper.invokeGetter(this, field);
            processValue(field.getName(), value, variables);
        }

        return variables;
    }

    private void processValue(String key, Object value, Map<String, Variable> variables) {
        if (Collection.class.isAssignableFrom(value.getClass())) {
            int index = 1;
            for (Object element : ((Collection) value))
                processValue(String.format("%s[%d]", key, index++), element, variables);
        } else
            variables.put(key, (Variable) value);
    }

    private Set<Field> getTypeAnnotatedFields() {
            Set<Field> fields = new LinkedHashSet<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            Type annotation = field.getAnnotation(Type.class);

            if (annotation != null)
                fields.add(field);
        }
        return fields;
    }

    /**
     * Empty default implementation to override by gadgets on demand.
     */
    @Override
    public void performChecks() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return Arrays.asList(Arrays.asList(this));
    }
}
