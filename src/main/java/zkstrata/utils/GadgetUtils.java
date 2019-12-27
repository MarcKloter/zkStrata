package zkstrata.utils;

import zkstrata.domain.data.types.Literal;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.exceptions.InternalCompilerException;

import java.math.BigInteger;

public class GadgetUtils {
    private GadgetUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isInstanceVariable(Variable variable) {
        return variable instanceof InstanceVariable;
    }

    public static boolean isWitnessVariable(Variable variable) {
        return variable instanceof WitnessVariable;
    }

    public static boolean isOfTypeBigInteger(Variable variable) {
        return variable.getType() == BigInteger.class;
    }

    public static void assertIsBigInteger(Variable variable) {
        if (!isOfTypeBigInteger(variable))
            throw new InternalCompilerException("Type mismatch. Found: %s, expected: BigInteger", variable.getType().getSimpleName());
    }

    public static InstanceVariable addOne(InstanceVariable variable) {
        if (variable == null)
            return null;

        assertIsBigInteger(variable);

        BigInteger value = (BigInteger) variable.getValue().getValue();
        return new InstanceVariable(new Literal(value.add(BigInteger.ONE)), variable.getReference(), variable.getPosition());
    }

    public static InstanceVariable subtractOne(InstanceVariable variable) {
        if (variable == null)
            return null;

        assertIsBigInteger(variable);

        BigInteger value = (BigInteger) variable.getValue().getValue();
        return new InstanceVariable(new Literal(value.subtract(BigInteger.ONE)), variable.getReference(), variable.getPosition());
    }
}
