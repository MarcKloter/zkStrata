package zkstrata.analysis;

import zkstrata.domain.data.types.wrapper.InstanceVariable;
import zkstrata.domain.data.types.wrapper.Variable;
import zkstrata.domain.data.types.wrapper.WitnessVariable;
import zkstrata.exceptions.CompileTimeException;

import java.util.List;

public class VariableExposure {
    private WitnessVariable witness = null;
    private InstanceVariable instance = null;

    public void mark(Variable variable) {
        if (variable instanceof WitnessVariable)
            this.witness = (WitnessVariable) variable;

        if (variable instanceof InstanceVariable)
            this.instance = (InstanceVariable) variable;

        this.checkExposure();
    }

    private void checkExposure() {
        if (witness != null && instance != null)
            throw new CompileTimeException("Value is being used as witness and instance data simultaneously.",
                    List.of(witness, instance));
    }
}
