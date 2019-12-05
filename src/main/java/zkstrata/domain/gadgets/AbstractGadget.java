package zkstrata.domain.gadgets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.TextStringBuilder;
import zkstrata.domain.data.types.Any;
import zkstrata.domain.data.types.wrapper.Null;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.optimizer.Substitution;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractGadget<T extends AbstractGadget> implements Gadget<T> {
    @Override
    public void initFrom(Map<String, Object> sourceFields) {
        for (Map.Entry<String, Object> source : sourceFields.entrySet()) {
            try {
                Field destination = this.getClass().getDeclaredField(source.getKey());
                Object destinationValue = source.getValue();
                checkType(destination, destinationValue);
                if (destinationValue.getClass() == Null.class)
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

    private void checkType(Field field, Object value) {
        if (value instanceof Variable)
            checkTypeAnnotation(field, (Variable) value);
        else if (!field.getType().isAssignableFrom(value.getClass()))
            throw new InternalCompilerException("Type mismatch for field %s in gadget %s and its AST predicate.",
                    field.getName(), field.getDeclaringClass().getSimpleName());
    }

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
    public List<Variable> getVariables() {
        List<Variable> variables = new ArrayList<>();

        for (Field field : getTypeAnnotatedFields()) {
            try {
                String fieldName = field.getName();
                Object value = new PropertyDescriptor(fieldName, this.getClass(), "get" +
                        StringUtils.capitalize(fieldName), null).getReadMethod().invoke(this);
                processValue(value, variables);
            } catch (IntrospectionException e) {
                throw new InternalCompilerException("Unable to call getter method for field %s in gadget %s. "
                        + "Ensure the gadget class defines public getter methods for all its fields.",
                        field.getName(), this.getClass());
            } catch (ReflectiveOperationException | ClassCastException e) {
                throw new InternalCompilerException("Invalid getter method for field %s in gadget %s.",
                        field.getName(), this.getClass().getSimpleName());
            }
        }

        return variables;
    }

    private void processValue(Object value, List<Variable> variables) {
        if (Collection.class.isAssignableFrom(value.getClass()))
            for (Object element : ((Collection) value))
                processValue(element, variables);
        else
            variables.add((Variable) value);
    }

    private Set<Field> getTypeAnnotatedFields() {
        Set<Field> fields = new HashSet<>();
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

    @Substitution(target = {AbstractGadget.class}, context = {AbstractGadget.class})
    public Optional<Set<Gadget>> removeDuplicate(T other) {
        return isEqualTo(other) ? Optional.empty() : Optional.of(Set.of(this));
    }

    @Override
    public String toString() {
        TextStringBuilder builder = new TextStringBuilder();
        builder.append("%s@%s", getClass().getSimpleName(), Integer.toHexString(hashCode()));
        getVariables().forEach(var -> builder.append(" <%s>", var.toString()));
        return builder.build();
    }

    @Override
    public String toDebugString() {
        TextStringBuilder builder = new TextStringBuilder();
        builder.appendln("%s@%s", getClass().getSimpleName(), Integer.toHexString(hashCode()));
        getVariables().forEach(var ->
                builder.appendln("    %s@%s: %s", var.getClass().getSimpleName(), Integer.toHexString(var.hashCode()), var.toString())
        );
        return builder.build();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        @SuppressWarnings("unchecked")
        boolean result = isEqualTo((T) obj);

        return result;
    }

    @Override
    public int hashCode() {
        return getVariables().stream().mapToInt(Variable::hashCode).sum();
    }

    @Override
    public List<List<Gadget>> getEvaluationPaths() {
        return Arrays.asList(Arrays.asList(this));
    }
}
