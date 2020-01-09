package zkstrata.analysis;

import zkstrata.domain.data.types.Literal;
import zkstrata.exceptions.CompileTimeException;
import zkstrata.exceptions.Position;
import zkstrata.exceptions.TypeCheckException;
import zkstrata.utils.Constants;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Set;

import static zkstrata.utils.ReflectionHelper.getMethodsAnnotatedWith;
import static zkstrata.utils.ReflectionHelper.invokeStaticMethod;

/**
 * This class enforces constraints placed on types defined by zkStrata.
 */
public class TypeConstraintsChecker {
    private static final Set<Method> TYPE_CONSTRAINTS = getMethodsAnnotatedWith(TypeConstraint.class);

    private TypeConstraintsChecker() {
        throw new IllegalStateException("Utility class");
    }

    public static void process(Literal literal, Position.Absolute position) {
        for (Method typeConstraint : TYPE_CONSTRAINTS) {
            Class<?> expectedType = typeConstraint.getAnnotation(TypeConstraint.class).value();

            if (literal.getType() == expectedType) {
                try {
                    invokeStaticMethod(typeConstraint, literal.getValue());
                } catch (TypeCheckException e) {
                    throw new CompileTimeException(e.getMessage(), position);
                }
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
    public static void constrainNumbersTo64BitUnsigned(BigInteger bigInteger) {
        if (bigInteger.compareTo(BigInteger.ZERO) < 0)
            throw new TypeCheckException("Negative numbers are not allowed.");

        if (bigInteger.compareTo(Constants.UNSIGNED_64BIT_MAX) > 0)
            throw new TypeCheckException("Number too large. Numbers cannot exceed 64 unsigned bits.");
    }
}
