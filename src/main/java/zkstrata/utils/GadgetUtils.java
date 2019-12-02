package zkstrata.utils;

import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;

import java.math.BigInteger;

public class GadgetUtils {
    public static boolean isInstanceVariable(Variable variable) {
        return variable instanceof InstanceVariable;
    }
    public static boolean isWitnessVariable(Variable variable) {
        return variable instanceof WitnessVariable;
    }

    public static boolean isBigInteger(Variable variable) {
        return variable.getType() == BigInteger.class;
    }
}
