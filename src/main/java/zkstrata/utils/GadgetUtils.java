package zkstrata.utils;

import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;

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
}
