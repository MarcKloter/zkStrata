package zkstrata.analysis;

import zkstrata.domain.data.types.Literal;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.InternalCompilerException;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.TypeCheckException;
import zkstrata.utils.Constants;
import zkstrata.utils.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Set;

/**
 * This class enforces constraints placed on types defined by zkStrata.
 */
public class TypeConstraintsChecker {
    private TypeConstraintsChecker() {
        throw new IllegalStateException("Utility class");
    }

    public static void process(Literal literal, Position.Absolute position) {
        Set<Method> typeConstraints = ReflectionHelper.getMethodsAnnotatedWith(TypeConstraint.class);

        for (Method typeConstraint : typeConstraints) {
            Class<?> expectedType = typeConstraint.getAnnotation(TypeConstraint.class).value();

            if (literal.getType() == expectedType)
                try {
                    typeConstraint.invoke(null, literal.getValue());
                } catch (InvocationTargetException e) {
                    throw new CompileTimeException(e.getTargetException().getMessage(), position);
                } catch (IllegalAccessException e) {
                    throw new InternalCompilerException("Error during invocation of @TypeConstraint annotated method " +
                            "%s in %s. Ensure that the method takes one argument of the specified type %s.",
                            typeConstraint.getName(), typeConstraint.getDeclaringClass(), expectedType.getSimpleName());
                }
        }
    }

    /**
     * Checks whether the given {@code bigInteger} is an unsigned 64 bit integer.
     * Throws an exception if the given {@link BigInteger} violates the constraint.
     *
     * @param bigInteger {@link BigInteger} to check
     */
    @TypeConstraint(BigInteger.class)
    private static void constrainNumbersTo64BitUnsigned(BigInteger bigInteger) throws TypeCheckException {
        if (bigInteger.compareTo(BigInteger.ZERO) < 0)
            throw new TypeCheckException("Negative numbers are not allowed.");

        if (bigInteger.compareTo(Constants.UNSIGNED_64BIT_MAX) > 0)
            throw new TypeCheckException("Number too large. Numbers cannot exceed 64 unsigned bits.");
    }
}
